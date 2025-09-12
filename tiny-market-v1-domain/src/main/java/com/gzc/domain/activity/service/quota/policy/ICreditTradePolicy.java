package com.gzc.domain.activity.service.quota.policy;

import com.gzc.domain.activity.model.aggregate.CreateQuotaOrderAggregate;

/**
 * 积分交易策略服务
 * 签到返利 - 无支付，积分兑换 - 有支付
 */
public interface ICreditTradePolicy {

    void creditTrade(CreateQuotaOrderAggregate aggregate);
}
