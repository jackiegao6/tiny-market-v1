package com.gzc.domain.strategy.service.rule.tree.factory.engine;

import com.gzc.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.gzc.domain.strategy.model.valobj.RuleTreeNodeLineVO;
import com.gzc.domain.strategy.model.valobj.RuleTreeNodeVO;
import com.gzc.domain.strategy.model.valobj.RuleTreeVO;
import com.gzc.domain.strategy.service.rule.tree.ILogicTreeNode;
import com.gzc.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description 决策树引擎
 */
@Slf4j
public class DecisionTreeEngine implements IDecisionTreeEngine {

    private final Map<String, ILogicTreeNode> logicTreeNodeGroup;

    private final RuleTreeVO ruleTreeVO;

    public DecisionTreeEngine(Map<String, ILogicTreeNode> logicTreeNodeGroup, RuleTreeVO ruleTreeVO) {
        this.logicTreeNodeGroup = logicTreeNodeGroup;
        this.ruleTreeVO = ruleTreeVO;
    }

    @Override
    public DefaultTreeFactory.StrategyAwardVO process(String userId, Long strategyId, Integer awardId, Date endDateTime) {
        DefaultTreeFactory.StrategyAwardVO strategyAwardData = null;

        // 获取基础信息
        String nextNodeKey = ruleTreeVO.getTreeRootRuleNode();
        Map<String, RuleTreeNodeVO> treeNodeMap = ruleTreeVO.getTreeNodeMap();

        // 获取起始节点「根节点记录了第一个要执行的规则」
        RuleTreeNodeVO ruleTreeNode = treeNodeMap.get(nextNodeKey);
        while (null != nextNodeKey) {
            // 获取决策节点
            ILogicTreeNode logicTreeNode = logicTreeNodeGroup.get(ruleTreeNode.getRuleKey());
            String ruleValue = ruleTreeNode.getRuleValue();

            // 决策节点计算
            DefaultTreeFactory.TreeActionEntity awardRuleEntity = logicTreeNode.logic(userId, strategyId, awardId, ruleValue, endDateTime);
            RuleLogicCheckTypeVO ruleLogicCheckTypeVO = awardRuleEntity.getRuleLogicCheckType();
            strategyAwardData = awardRuleEntity.getStrategyAwardVO();

            // 获取下个节点
            nextNodeKey = nextNode(ruleLogicCheckTypeVO.getCode(), ruleTreeNode.getTreeNodeLineVOList());
            ruleTreeNode = treeNodeMap.get(nextNodeKey);
        }

        // 返回最终结果
        return strategyAwardData;
    }

    public String nextNode(String matterValue, List<RuleTreeNodeLineVO> treeNodeLineVOList) {
        if (null == treeNodeLineVOList || treeNodeLineVOList.isEmpty())
            return null;

        for (RuleTreeNodeLineVO nodeLine : treeNodeLineVOList) {
            if (decisionLogic(matterValue, nodeLine)) {
                return nodeLine.getRuleNodeTo();
            }
        }
        return null;
    }

    public boolean decisionLogic(String matterValue, RuleTreeNodeLineVO nodeLine) {
        switch (nodeLine.getRuleLimitType()) {
            case EQUAL:
                return matterValue.equals(nodeLine.getRuleLimitValue().getCode());
            // 以下规则暂时不需要实现
            case GT:
            case LT:
            case GE:
            case LE:
            default:
                return false;
        }
    }

}
