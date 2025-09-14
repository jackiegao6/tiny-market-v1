package com.gzc.domain.strategy.service.rule.tree.impl;

import com.gzc.domain.strategy.adapter.repository.IStrategyRepository;
import com.gzc.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.gzc.domain.strategy.service.rule.tree.ILogicTreeNode;
import com.gzc.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @description 次数锁节点
 */
@Slf4j
@Component("rule_lock")
public class RuleLockLogicTreeNode implements ILogicTreeNode {

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId,String ruleValue, Date endDateTime) {
        log.info("规则过滤-次数锁 userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);

        int raffleCount = 0;
        try {
            raffleCount = Integer.parseInt(ruleValue);
        } catch (Exception e) {
            throw new RuntimeException("规则过滤-次数锁异常 ruleValue: " + ruleValue + " 配置不正确");
        }

        // 查询用户当天的抽奖次数
        Integer userRaffleCount = strategyRepository.queryTodayUserRaffleCount(userId, strategyId);

        // 用户抽奖次数大于规则限定值，规则放行
        if (userRaffleCount >= raffleCount) {
            log.info("规则过滤-次数锁【放行】 userId:{} strategyId:{} awardId:{} raffleCount:{} userRaffleCount:{}", userId, strategyId, awardId, userRaffleCount, userRaffleCount);
            return DefaultTreeFactory.TreeActionEntity.builder()
                    .ruleLogicCheckType(RuleLogicCheckTypeVO.ALLOW)
                    .build();
        }

        // 用户抽奖次数小于规则限定值，则该奖品暂时不能让你拿到
        return DefaultTreeFactory.TreeActionEntity.builder()
                .ruleLogicCheckType(RuleLogicCheckTypeVO.TAKE_OVER)
                .build();
    }

}
