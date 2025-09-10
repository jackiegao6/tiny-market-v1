package com.gzc.domain.rebate.service;

import com.gzc.domain.rebate.model.entity.BehaviorEntity;

import java.util.List;

public interface IBehaviorRebateService {

    List<String> createRebateOrder(BehaviorEntity behaviorEntity);
}
