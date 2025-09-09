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
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 策略装配库(兵工厂)，负责初始化策略计算
 * @create 2023-12-23 10:02
 */
@Slf4j
@Service
public class StrategyArmoryDispatch implements IStrategyArmory, IStrategyDispatch {

    @Resource
    private IStrategyRepository repository;

    @Override
    public boolean assembleLotteryStrategyByActivityId(Long activityId) {
        Long strategyId = repository.queryStrategyIdByActivityId(activityId);

        return assembleLotteryStrategy(strategyId);
    }

    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        // 1. 查询策略配置
        List<StrategyAwardEntity> strategyAwards = repository.queryStrategyAwardList(strategyId);
        if (strategyAwards == null || strategyAwards.isEmpty()) return false;

        assembleLotteryStrategy(String.valueOf(strategyId), strategyAwards);

        // 2. 权重规则配置
        StrategyEntity strategyEntity = repository.queryStrategyEntityByStrategyId(strategyId);
        String ruleWeight = strategyEntity.getRuleWeight();
        if (null == ruleWeight){
            // 不含有权重规则配置
            return true;
        }
        StrategyRuleEntity strategyRuleEntity = repository.queryStrategyRuleEntityByStrategyId(strategyId, ruleWeight);
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
            assembleLotteryStrategy(String.valueOf(strategyId).concat("_").concat(weightThreshold), deepCopyStrategyAwards);
        }
        return true;
    }

    private void assembleLotteryStrategy(String armoryAwardsKey, List<StrategyAwardEntity> strategyAwardEntities){
        // 1. 如果已经装配 就return
        if(repository.hasStrategyAwardSearchRateTable(armoryAwardsKey)){
            return;
        }
        assembleLotteryAward(armoryAwardsKey, strategyAwardEntities);
        // 2. 获取最小概率值
        BigDecimal minAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 3. 获取概率值总和
        BigDecimal totalAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 用 1 % 0.0001 获得概率范围，百分位、千分位、万分位
        BigDecimal rateRange = totalAwardRate.divide(minAwardRate, 0, RoundingMode.CEILING);

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

        // 6. 对存储的奖品进行乱序操作
        Collections.shuffle(strategyAwardSearchRateList);

        // 7. 生成出Map集合，key值，对应的就是后续的概率值。通过概率来获得对应的奖品ID
        Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = new LinkedHashMap<>();
        for (int i = 0; i < strategyAwardSearchRateList.size(); i++) {
            shuffleStrategyAwardSearchRateTable.put(i, strategyAwardSearchRateList.get(i));
        }

        // 8. 存放到 Redis
        repository.storeStrategyAwardSearchRateTable(armoryAwardsKey, shuffleStrategyAwardSearchRateTable.size(), shuffleStrategyAwardSearchRateTable);
    }

    private void assembleLotteryAward(String armoryAwardsKey, List<StrategyAwardEntity> strategyAwardEntities) {

        for (StrategyAwardEntity entity : strategyAwardEntities) {
            Integer awardId = entity.getAwardId();
            Integer awardCount = entity.getAwardCountSurplus();

            String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_KEY + armoryAwardsKey + Constants.COLON + awardId;
            repository.cacheLotteryAward(cacheKey, awardCount);
        }
    }

    @Override
    public Integer getRandomAwardId(Long strategyId) {
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取。
        int rateRange = repository.getRateRange(strategyId);
        // 通过生成的随机值，获取概率值奖品查找表的结果
        return repository.getRandomAward(strategyId, new SecureRandom().nextInt(rateRange));
    }

    @Override
    public Integer getRandomAwardId(Long strategyId, String ruleWeightValue) {
        String key = String.valueOf(strategyId).concat("_").concat(ruleWeightValue);
        int rateRange = repository.getRateRange(key);
        return repository.getRandomAward(key, new SecureRandom().nextInt(rateRange));
    }

    @Override
    public Boolean subtractionAwardStock(Long strategyId, Integer awardId) {
        return subtractionAwardStock(String.valueOf(strategyId), awardId);
    }

    private Boolean subtractionAwardStock(String armoryAwardsKey, Integer awardId){
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_KEY + armoryAwardsKey + Constants.COLON + awardId;
        return repository.subtractionAwardStock(cacheKey, awardId);
    }
}
