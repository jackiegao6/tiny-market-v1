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

@Service
@RequiredArgsConstructor
public class CreditAdjustService implements ICreditAdjustService {

    private final ICreditRepository creditRepository;
    private final CreditAdjustSuccessMessageEvent creditAdjustSuccessMessageEvent;


    @Override
    public String createCreditOrder(CreditTradeEntity tradeEntity) {

        // 1. 创建账户积分实体
        CreditAccountEntity creditAccountEntity = TradeAggregate.createCreditAccountEntity(
                tradeEntity.getUserId(),
                tradeEntity.getAmount());

        // 2. 创建账户订单实体
        CreditOrderEntity creditOrderEntity = TradeAggregate.createCreditOrderEntity(
                tradeEntity.getUserId(),
                tradeEntity.getTradeName(),
                tradeEntity.getTradeType(),
                tradeEntity.getAmount(),
                tradeEntity.getOutBusinessNo());
        // 3. 构建消息任务对象
        CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage creditAdjustSuccessMessage = new CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage();
        creditAdjustSuccessMessage.setUserId(tradeEntity.getUserId());
        creditAdjustSuccessMessage.setOrderId(creditOrderEntity.getOrderId());
        creditAdjustSuccessMessage.setAmount(tradeEntity.getAmount());
        creditAdjustSuccessMessage.setOutBusinessNo(tradeEntity.getOutBusinessNo());
        BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage> creditAdjustSuccessEventMessage = creditAdjustSuccessMessageEvent.buildEventMessage(creditAdjustSuccessMessage);

        CreditAdjustTaskEntity taskEntity = TradeAggregate.createCreditAdjustTaskEntity(tradeEntity.getUserId(), creditAdjustSuccessMessageEvent.topic(), creditAdjustSuccessEventMessage.getId(), creditAdjustSuccessEventMessage);

        // 4. 构建交易聚合对象
        TradeAggregate tradeAggregate = TradeAggregate.builder()
                .userId(tradeEntity.getUserId())
                .creditAccountEntity(creditAccountEntity)
                .creditOrderEntity(creditOrderEntity)
                .creditAdjustTaskEntity(taskEntity)
                .build();

        // 5. 保存积分交易订单
        creditRepository.saveUserCreditTradeOrder(tradeAggregate);

        return creditOrderEntity.getOrderId();
    }
}
