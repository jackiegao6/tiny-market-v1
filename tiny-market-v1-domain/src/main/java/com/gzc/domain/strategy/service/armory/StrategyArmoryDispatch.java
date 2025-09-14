package com.gzc.domain.strategy.service.armory;

import com.gzc.domain.strategy.adapter.repository.IStrategyRepository;
import com.gzc.domain.strategy.model.entity.StrategyAwardEntity;
import com.gzc.domain.strategy.model.entity.StrategyEntity;
import com.gzc.domain.strategy.model.entity.StrategyRuleEntity;
import com.gzc.domain.strategy.service.armory.algorithm.AbstractAlgorithm;
import com.gzc.domain.strategy.service.armory.algorithm.IAlgorithm;
import com.gzc.types.common.Constants;
import com.gzc.types.enums.ResponseCode;
import com.gzc.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @description 策略装配库(兵工厂)，负责初始化策略计算
 */
@Slf4j
@Service
public class StrategyArmoryDispatch implements IStrategyArmory, IStrategyDispatch {

    @Resource
    private IStrategyRepository strategyRepository;
    @Resource
    private Map<String, IAlgorithm> algorithmMap;
    // map容量大于 ALGORITHM_THRESHOLD_VALUE 转为算法二 (格子区间)
    private final Integer ALGORITHM_THRESHOLD_VALUE = 10000;

    @Override
    public boolean assembleLotteryStrategyByActivityId(Long activityId) {
        // 根据活动id 找到 1v1 的策略id
        Long strategyId = strategyRepository.queryStrategyIdByActivityId(activityId);
        return assembleLotteryStrategy(strategyId);
    }

    /**
     * 装配策略相关的信息
     */
    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        // 1. 查询&缓存策略配置的奖品列表
        List<StrategyAwardEntity> strategyAwards = strategyRepository.queryStrategyAwardList(strategyId);
        if (strategyAwards == null || strategyAwards.isEmpty())
            return false;

        cacheStrategyAwards(String.valueOf(strategyId), strategyAwards);

        // 2. 权重规则配置
        StrategyEntity strategyEntity = strategyRepository.queryStrategyEntityByStrategyId(strategyId);
        String ruleWeight = strategyEntity.getRuleWeight();
        if (null == ruleWeight){
            // 不含有权重规则配置
            return true;
        }

        // 3. 缓存权重策略配置的奖品列表
        StrategyRuleEntity strategyRuleEntity = strategyRepository.queryStrategyRuleEntityByStrategyId(strategyId, ruleWeight);
        if (null == strategyRuleEntity){
            throw new AppException(ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getCode(), ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getInfo());
        }

        Map<String, List<Integer>> weightMap = strategyRuleEntity.getRuleWeightValues();
        Set<String> weightThresholds = weightMap.keySet();
        for (String weightThreshold : weightThresholds) {
            List<Integer> displayAwardIds = weightMap.get(weightThreshold);
            // 深拷贝 原来所有的奖品
            ArrayList<StrategyAwardEntity> deepCopyStrategyAwards = new ArrayList<>(strategyAwards);
            deepCopyStrategyAwards.removeIf(e -> !displayAwardIds.contains(e.getAwardId()));
            cacheStrategyAwards(String.valueOf(strategyId).concat("_").concat(weightThreshold), deepCopyStrategyAwards);
        }
        return true;
    }

    /**
     * 生成概率查找表、奖品个数、各奖品的库存信息 并放入redis中
     */
    private void cacheStrategyAwards(String strategyKey, List<StrategyAwardEntity> strategyAwardEntities){
        // 0. 如果已经装配概率查找表 就return
        if(strategyRepository.hasSearchRateTable(strategyKey)){
            return;
        }
        // 1. 缓存 各奖品的库存信息
        for (StrategyAwardEntity entity : strategyAwardEntities) {
            Integer awardId = entity.getAwardId();
            Integer awardCountSurplus = entity.getAwardCountSurplus();

            String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_KEY + strategyKey + Constants.COLON + awardId;
            strategyRepository.cacheLotteryAward(cacheKey, awardCountSurplus);
        }

        // 2. 获取最小概率值
        BigDecimal minRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 3. 获取概率值总和
        BigDecimal totalRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 用 1 % 0.0001 获得概率范围，百分位、千分位、万分位
        BigDecimal rateRange = totalRate.divide(minRate, 0, RoundingMode.CEILING);

        if (rateRange.intValue() <= ALGORITHM_THRESHOLD_VALUE){
            String name = AbstractAlgorithm.Algorithm.O1.getName();
            algorithmMap.get(name).armoryAlgorithm(strategyKey, strategyAwardEntities, rateRange);
            strategyRepository.cacheAlgorithmKey(name);
        }else {
            String name = AbstractAlgorithm.Algorithm.OLogN.getName();
            algorithmMap.get(name).armoryAlgorithm(strategyKey, strategyAwardEntities, rateRange);
            strategyRepository.cacheAlgorithmKey(name);
        }
    }

    @Override
    public Integer getRandomAwardId(Long strategyId) {
        String name = strategyRepository.cacheAlgorithmKey(String.valueOf(strategyId));
        return algorithmMap.get(name).raffleAlgorithm(String.valueOf(strategyId));
    }

    @Override
    public Integer getRandomAwardId(Long strategyId, String ruleWeightValue) {
        String key = String.valueOf(strategyId).concat("_").concat(ruleWeightValue);
        String name = strategyRepository.cacheAlgorithmKey(key);
        return algorithmMap.get(name).raffleAlgorithm(String.valueOf(strategyId));
    }

    @Override
    public Boolean subtractionAwardStock(Long strategyId, Integer awardId, Date endDateTime) {
        return subtractionAwardStock(String.valueOf(strategyId), awardId);
    }

    private Boolean subtractionAwardStock(String armoryAwardsKey, Integer awardId){
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_KEY + armoryAwardsKey + Constants.COLON + awardId;
        return strategyRepository.subtractionAwardStock(cacheKey, awardId);
    }

    private Boolean subtractionAwardStock(String armoryAwardsKey, Integer awardId, Date endDateTime){
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_KEY + armoryAwardsKey + Constants.COLON + awardId;
        return strategyRepository.subtractionAwardStock(cacheKey, awardId, endDateTime);
    }
}
