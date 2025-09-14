package com.gzc.domain.strategy.service.rule.tree.factory;

import com.gzc.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.gzc.domain.strategy.model.valobj.RuleTreeVO;
import com.gzc.domain.strategy.service.rule.tree.ILogicTreeNode;
import com.gzc.domain.strategy.service.rule.tree.factory.engine.DecisionTreeEngine;
import com.gzc.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @description 规则树工厂
 */
@Service
public class DefaultTreeFactory {

    private final Map<String, ILogicTreeNode> logicTreeNodeGroup;

    public DefaultTreeFactory(Map<String, ILogicTreeNode> logicTreeNodeGroup) {
        this.logicTreeNodeGroup = logicTreeNodeGroup;
    }

    public IDecisionTreeEngine openLogicTree(RuleTreeVO ruleTreeVO) {
        return new DecisionTreeEngine(logicTreeNodeGroup, ruleTreeVO);
    }

    /**
     * 决策树动作实体
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TreeActionEntity {
        private RuleLogicCheckTypeVO ruleLogicCheckType;
        private StrategyAwardVO strategyAwardVO;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StrategyAwardVO {
        /**
         * 抽奖奖品ID - 内部流转使用
         */
        private Integer awardId;
        /**
         * 抽奖奖品规则
         */
        private String awardRuleValue;
    }

}
