package com.gzc.domain.credit.adapter.repository;

import com.gzc.domain.credit.model.aggregate.TradeAggregate;
import com.gzc.domain.credit.model.entity.CreditAccountEntity;

/**
 * @description 用户积分仓储
 */
public interface ICreditRepository {

    void adjustCreditAccount(TradeAggregate tradeAggregate);

    CreditAccountEntity queryUserCreditAccount(String userId);

}
