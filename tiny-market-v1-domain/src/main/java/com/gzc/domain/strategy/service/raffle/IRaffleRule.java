package com.gzc.domain.strategy.service.raffle;

import com.gzc.domain.strategy.model.valobj.RuleWeightVO;

import java.util.List;
import java.util.Map;

public interface IRaffleRule {

    /**
     * 根据规则树id 查询加锁的的值[部分奖品需要抽奖N次后解锁]
     *
     * @param treeIds 规则树id值
     * @return map key: 规则树id value: 加锁值
     */
    Map<String, Integer> queryTreeLockCount(String[] treeIds);

    List<RuleWeightVO> queryRuleWeightDetails(Long strategyId);

    List<RuleWeightVO> queryRuleWeightDetailsByActivityId(Long activityId);

}
