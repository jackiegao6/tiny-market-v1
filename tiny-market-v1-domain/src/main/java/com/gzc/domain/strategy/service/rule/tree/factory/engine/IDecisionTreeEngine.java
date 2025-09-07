package com.gzc.domain.strategy.service.rule.tree.factory.engine;

import com.gzc.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;

/**
 * @description 规则树组合接口
 */
public interface IDecisionTreeEngine {

    DefaultTreeFactory.StrategyAwardData process(String userId, Long strategyId, Integer awardId);

}
