package com.gzc.domain.rebate.service;

import com.gzc.domain.rebate.model.entity.BehaviorEntity;
import com.gzc.domain.rebate.model.entity.BehaviorRebateOrderEntity;

import java.util.List;

public interface IBehaviorRebateService {

    List<String> createRebateOrder(BehaviorEntity behaviorEntity);

    List<BehaviorRebateOrderEntity> queryOrderByOutBusinessNo(String userId, String outBusinessNo);
}
