package com.gzc.domain.strategy.adapter.repository;


import com.gzc.domain.strategy.model.entity.StrategyAwardEntity;
import com.gzc.domain.strategy.model.entity.StrategyEntity;
import com.gzc.domain.strategy.model.entity.StrategyRuleEntity;

import java.util.List;
import java.util.Map;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 策略服务仓储接口
 * @create 2023-12-23 09:33
 */
public interface IStrategyRepository {

    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    void storeStrategyAwardSearchRateTable(String armoryAwardsKey, Integer rateRange, Map<Integer, Integer> strategyAwardSearchRateTable);

    Integer getRandomAward(Long strategyId, Integer rateKey);

    Integer getRandomAward(String key, Integer rateKey);

    int getRateRange(Long strategyId);

    int getRateRange(String key);

    StrategyEntity queryStrategyEntityByStrategyId(Long strategyId);

    StrategyRuleEntity queryStrategyRuleEntityByStrategyId(Long strategyId, String ruleModel);

    boolean hasStrategyAwardSearchRateTable(String armoryAwardsKey);

}
