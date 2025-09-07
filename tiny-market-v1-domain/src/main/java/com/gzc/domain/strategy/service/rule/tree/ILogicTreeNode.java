package com.gzc.domain.strategy.service.rule.tree;


import com.gzc.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;

/**
 * @description 规则树接口
 */
public interface ILogicTreeNode {

    DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId, String ruleValue);

}
