package com.gzc.domain.rebate.repository;

import com.gzc.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import com.gzc.domain.rebate.model.valobj.BehaviorVO;
import com.gzc.domain.rebate.model.valobj.DailyBehaviorRebateVO;

import java.util.List;

public interface IBehaviorRebateRepository {

    void saveUserRebateRecord(String userId, List<BehaviorRebateAggregate> behaviorRebateAggregates);

    List<DailyBehaviorRebateVO> queryDailyBehaviorRebateConfig(BehaviorVO behaviorVO);

}
