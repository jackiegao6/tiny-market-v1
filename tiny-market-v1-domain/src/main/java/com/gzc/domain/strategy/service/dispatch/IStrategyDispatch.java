package com.gzc.domain.strategy.service.dispatch;

public interface IStrategyDispatch {

    /**
     * 获取抽奖策略装配的随机结果
     *
     * @param strategyId 策略ID
     * @return 抽奖结果
     */
    Integer getRandomAwardId(Long strategyId);

    Integer getRandomAwardId(Long strategyId, String ruleWeightValue);

    /**
     * 根据策略id 和 奖品id扣减奖品缓存库存
     */
    Boolean subtractionAwardStock(Long strategyId, Integer awardId);
}
