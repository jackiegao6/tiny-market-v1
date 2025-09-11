package com.gzc.domain.credit.adapter.repository;

import com.gzc.domain.credit.model.aggregate.TradeAggregate;

/**
 * @description 用户积分仓储
 */
public interface ICreditRepository {

    void saveUserCreditTradeOrder(TradeAggregate tradeAggregate);

}
