package com.gzc.domain.strategy.service.raffle;

import com.gzc.domain.strategy.adapter.repository.IStrategyRepository;
import com.gzc.domain.strategy.model.entity.StrategyAwardEntity;
import com.gzc.domain.strategy.model.valobj.RuleTreeVO;
import com.gzc.domain.strategy.model.valobj.RuleWeightVO;
import com.gzc.domain.strategy.model.valobj.StrategyAwardRuleModelVO;
import com.gzc.domain.strategy.model.valobj.StrategyAwardStockKeyVO;
import com.gzc.domain.strategy.service.dispatch.IStrategyDispatch;
import com.gzc.domain.strategy.service.rule.chain.ILogicChain;
import com.gzc.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.gzc.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import com.gzc.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description 默认的抽奖策略实现
 */
@Slf4j
@Service
public class DefaultRaffleStrategyService extends AbstractRaffleStrategy implements IRaffleStock, IRaffleAwardService, IRaffleRule {

    public DefaultRaffleStrategyService(IStrategyRepository repository, IStrategyDispatch strategyDispatch, DefaultChainFactory defaultChainFactory, DefaultTreeFactory defaultTreeFactory) {
        super(repository, strategyDispatch, defaultChainFactory, defaultTreeFactory);
    }

    @Override
    public DefaultChainFactory.StrategyAwardVO raffleLogicChain(String userId, Long strategyId) {
        ILogicChain logicChain = defaultChainFactory.openLogicChain(strategyId);
        return logicChain.logic(userId, strategyId);
    }

    @Override
    public DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Integer awardId, Long strategyId, Date endDateTime) {
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = strategyRepository.queryStrategyAwardRuleModelVO(strategyId, awardId);
        if (null == strategyAwardRuleModelVO) {
            return DefaultTreeFactory.StrategyAwardVO.builder()
                    .awardId(awardId)
                    .build();
        }
        RuleTreeVO ruleTreeVO = strategyRepository.queryRuleTreeVOByTreeId(strategyAwardRuleModelVO.getRuleModels());
        if (null == ruleTreeVO) {
            throw new RuntimeException("存在抽奖策略配置的规则模型 Key，未在库表 rule_tree、rule_tree_node、rule_tree_line 配置对应的规则树信息 " + strategyAwardRuleModelVO.getRuleModels());
        }
        IDecisionTreeEngine treeEngine = defaultTreeFactory.openLogicTree(ruleTreeVO);
        return treeEngine.process(userId, strategyId, awardId, endDateTime);
    }

    @Override
    public DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Integer awardId, Long strategyId) {
        return raffleLogicTree(userId, awardId, strategyId, null);
    }

    @Override
    public StrategyAwardStockKeyVO takeQueueValue() {
        return strategyRepository.takeQueueValue();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        strategyRepository.updateStrategyAwardStock(strategyId, awardId);
    }

    @Override
    public List<StrategyAwardEntity> queryRaffleStrategyAwardList(Long strategyId) {
        return strategyRepository.queryStrategyAwardList(strategyId);
    }
    @Override
    public List<StrategyAwardEntity> queryRaffleStrategyAwardListByActivityId(Long activityId) {
        Long strategyId = strategyRepository.queryStrategyIdByActivityId(activityId);
        return strategyRepository.queryStrategyAwardList(strategyId);
    }

    @Override
    public Map<String, Integer> queryTreeLockCount(String[] treeIds) {
        return strategyRepository.queryTreeLockCount(treeIds);
    }

    @Override
    public List<RuleWeightVO> queryRuleWeightDetailsByActivityId(Long activityId) {
        Long strategyId = strategyRepository.queryStrategyIdByActivityId(activityId);
        return queryRuleWeightDetails(strategyId);
    }

    @Override
    public List<RuleWeightVO> queryRuleWeightDetails(Long strategyId) {
        return strategyRepository.queryRuleWeightDetails(strategyId);
    }
}
