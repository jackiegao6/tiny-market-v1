package com.gzc.domain.activity.service.quota.policy;

import com.gzc.domain.activity.adapter.repository.IActivityRepository;
import com.gzc.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import com.gzc.domain.activity.model.valobj.OrderStateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("credit_pay_trade")
@RequiredArgsConstructor
public class CreditPayTradePolicy implements ICreditTradePolicy {

    private final IActivityRepository activityRepository;

    @Override
    public void creditTrade(CreateQuotaOrderAggregate createQuotaOrderAggregate) {
        createQuotaOrderAggregate.setOrderState(OrderStateVO.wait_pay);
        activityRepository.doSaveCreditPayOrder(createQuotaOrderAggregate);
    }
}
