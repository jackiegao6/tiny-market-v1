package com.gzc.domain.strategy.service.armory;

import com.gzc.domain.strategy.adapter.repository.IStrategyRepository;
import com.gzc.domain.strategy.model.entity.StrategyAwardEntity;
import com.gzc.domain.strategy.model.entity.StrategyEntity;
import com.gzc.domain.strategy.model.entity.StrategyRuleEntity;
import com.gzc.domain.strategy.service.dispatch.IStrategyDispatch;
import com.gzc.types.common.Constants;
import com.gzc.types.enums.ResponseCode;
import com.gzc.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;

/**
 * @description 策略装配库(兵工厂)，负责初始化策略计算
 */
@Slf4j
@Service
public class StrategyArmoryDispatch implements IStrategyArmory, IStrategyDispatch {

    @Resource
    private IStrategyRepository strategyRepository;

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

        // 5. 生成策略奖品概率查找表「这里指需要在list集合中，存放上对应的奖品占位即可，占位越多等于概率越高」
        List<Integer> strategyAwardSearchRateList = new ArrayList<>(rateRange.intValue());
        for (StrategyAwardEntity strategyAward : strategyAwardEntities) {
            Integer awardId = strategyAward.getAwardId();
            BigDecimal awardRate = strategyAward.getAwardRate();
            // 计算出每个概率值需要存放到查找表的数量，循环填充
            for (int i = 0; i < rateRange.multiply(awardRate).setScale(0, RoundingMode.CEILING).intValue(); i++) {
                strategyAwardSearchRateList.add(awardId);
            }
        }
        Collections.shuffle(strategyAwardSearchRateList);

        Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = new HashMap<>();
        for (int i = 0; i < strategyAwardSearchRateList.size(); i++) {
            shuffleStrategyAwardSearchRateTable.put(i, strategyAwardSearchRateList.get(i));
        }

        // 6. 存放到 Redis
        strategyRepository.storeSearchRateTable(strategyKey, shuffleStrategyAwardSearchRateTable.size(), shuffleStrategyAwardSearchRateTable);
    }



    @Override
    public Integer getRandomAwardId(Long strategyId) {
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取。
        int rateRange = strategyRepository.getRateRange(strategyId);
        // 通过生成的随机值，获取概率值奖品查找表的结果
        return strategyRepository.getRandomAward(strategyId, new SecureRandom().nextInt(rateRange));
    }

    @Override
    public Integer getRandomAwardId(Long strategyId, String ruleWeightValue) {
        String key = String.valueOf(strategyId).concat("_").concat(ruleWeightValue);
        int rateRange = strategyRepository.getRateRange(key);
        return strategyRepository.getRandomAward(key, new SecureRandom().nextInt(rateRange));
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
