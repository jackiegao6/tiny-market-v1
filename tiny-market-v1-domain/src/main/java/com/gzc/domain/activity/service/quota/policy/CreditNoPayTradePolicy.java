package com.gzc.domain.activity.service.quota.policy;

import com.gzc.domain.activity.adapter.repository.IActivityRepository;
import com.gzc.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import com.gzc.domain.activity.model.valobj.OrderStateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service("rebate_no_pay_trade")
public class CreditNoPayTradePolicy implements ICreditTradePolicy {

    private final IActivityRepository activityRepository;

    @Override
    public void creditTrade(CreateQuotaOrderAggregate createQuotaOrderAggregate) {
        createQuotaOrderAggregate.setOrderState(OrderStateVO.completed);
        createQuotaOrderAggregate.getActivityOrderEntity().setPayAmount(BigDecimal.ZERO);

        activityRepository.doSaveSkuRechargeOrder(createQuotaOrderAggregate);
    }
}
