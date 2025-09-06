package com.gzc.domain.strategy.service.rule.impl;


import com.gzc.domain.strategy.adapter.repository.IStrategyRepository;
import com.gzc.domain.strategy.model.entity.RuleActionEntity;
import com.gzc.domain.strategy.model.entity.RuleMatterEntity;
import com.gzc.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.gzc.domain.strategy.service.annotation.LogicStrategy;
import com.gzc.domain.strategy.service.rule.ILogicFilter;
import com.gzc.domain.strategy.service.rule.factory.DefaultLogicFactory;
import com.gzc.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description 【抽奖前规则】黑名单用户过滤规则
 */
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_BLACKLIST)
public class RuleBackListLogicFilter implements ILogicFilter<RuleActionEntity.RaffleBeforeEntity> {

    @Resource
    private IStrategyRepository repository;

    @Override
    public RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> filter(RuleMatterEntity ruleMatterEntity) {
        String userId = ruleMatterEntity.getUserId();
        Long strategyId = ruleMatterEntity.getStrategyId();
        Integer awardId = ruleMatterEntity.getAwardId();
        String ruleModel = ruleMatterEntity.getRuleModel();

        String ruleValue = repository.queryStrategyRuleValue(strategyId, awardId, ruleModel);
        // 100:user01,user02
        String[] splitRuleValue = ruleValue.split(Constants.COLON);
        Integer blackAwardId = Integer.parseInt(splitRuleValue[0]);

        String[] userBlackIds = splitRuleValue[1].split(Constants.SPLIT);
        for (String userBlackId : userBlackIds) {
            if (userId.equals(userBlackId)) {
                return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                        .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())
                        .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())
                        .ruleModel(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode())
                        .data(RuleActionEntity.RaffleBeforeEntity.builder()
                                .strategyId(ruleMatterEntity.getStrategyId())
                                .awardId(blackAwardId)
                                .build())
                        .build();
            }
        }

        return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                .build();
    }
}
