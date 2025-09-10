package com.gzc.domain.rebate.model.aggregate;

import com.gzc.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import com.gzc.domain.rebate.model.entity.BehaviorRebateTaskEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BehaviorRebateAggregate {

    private BehaviorRebateOrderEntity behaviorRebateOrderEntity;
    private BehaviorRebateTaskEntity behaviorRebateTaskEntity;
}
