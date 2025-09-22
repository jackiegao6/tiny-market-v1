package com.gzc.domain.credit.service.impl;

import com.gzc.domain.credit.adapter.repository.ICreditRepository;
import com.gzc.domain.credit.event.CreditAdjustSuccessMessageEvent;
import com.gzc.domain.credit.model.aggregate.TradeAggregate;
import com.gzc.domain.credit.model.entity.CreditAccountEntity;
import com.gzc.domain.credit.model.entity.CreditAdjustTaskEntity;
import com.gzc.domain.credit.model.entity.CreditOrderEntity;
import com.gzc.domain.credit.model.entity.CreditTradeEntity;
import com.gzc.domain.credit.service.ICreditAdjustService;
import com.gzc.types.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CreditAdjustService implements ICreditAdjustService {

    private final ICreditRepository creditRepository;
    private final CreditAdjustSuccessMessageEvent creditAdjustSuccessMessageEvent;


    @Override
    public String createCreditOrder(CreditTradeEntity creditTradeEntity) {

        String userId = creditTradeEntity.getUserId();
        BigDecimal amount = creditTradeEntity.getAmount();
        // 1. 创建账户积分实体
        CreditAccountEntity creditAccountEntity = TradeAggregate.createCreditAccountEntity(
                userId,
                amount);

        // 2. 创建账户订单实体
        String outBusinessNo = creditTradeEntity.getOutBusinessNo();
        CreditOrderEntity creditOrderEntity = TradeAggregate.createCreditOrderEntity(
                userId,
                creditTradeEntity.getTradeName(),
                creditTradeEntity.getTradeType(),
                amount,
                outBusinessNo);
        // 3. 构建消息任务对象
        CreditAdjustSuccessMessageEvent.CreditAdjustMessage msgBody = CreditAdjustSuccessMessageEvent.CreditAdjustMessage.builder()
                .userId(userId)
                .orderId(creditOrderEntity.getOrderId())
                .amount(amount)
                .outBusinessNo(outBusinessNo)
                .build();
        BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustMessage> creditAdjustSuccessEventMessage = creditAdjustSuccessMessageEvent.buildEventMessage(msgBody);

        CreditAdjustTaskEntity taskEntity = TradeAggregate.createCreditAdjustTaskEntity(creditTradeEntity.getUserId(), creditAdjustSuccessMessageEvent.topic(), creditAdjustSuccessEventMessage.getId(), creditAdjustSuccessEventMessage);

        // 4. 构建交易聚合对象
        TradeAggregate tradeAggregate = TradeAggregate.builder()
                .userId(creditTradeEntity.getUserId())
                .creditAccountEntity(creditAccountEntity)
                .creditOrderEntity(creditOrderEntity)
                .creditAdjustTaskEntity(taskEntity)
                .build();

        // 5. 保存积分交易订单
        creditRepository.adjustCreditAccount(tradeAggregate);

        return creditOrderEntity.getOrderId();
    }


    @Override
    public CreditAccountEntity queryUserCreditAccount(String userId) {
        return creditRepository.queryUserCreditAccount(userId);

    }
}
