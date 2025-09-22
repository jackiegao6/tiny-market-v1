package com.gzc.domain.credit.event;

import com.gzc.domain.credit.model.valobj.TradeNameVO;
import com.gzc.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description 积分账户调整成功消息【充值、支付，成功消息】
 */
@Component
public class CreditAdjustSuccessMessageEvent extends BaseEvent<CreditAdjustSuccessMessageEvent.CreditAdjustMessage> {

    @Value("${spring.rabbitmq.topic.credit_adjust_success}")
    private String topic;

    @Override
    public EventMessage<CreditAdjustMessage> buildEventMessage(CreditAdjustMessage data) {
        return EventMessage.<CreditAdjustSuccessMessageEvent.CreditAdjustMessage>builder()
                .id(RandomStringUtils.randomNumeric(11))
                .timestamp(new Date())
                .data(data)
                .build();
    }

    @Override
    public String topic() {
        return topic;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreditAdjustMessage {

        /**
         * 用户ID
         */
        private String userId;
        /**
         * 订单ID
         */
        private String orderId;
        /**
         * 交易金额
         */
        private BigDecimal amount;
        /**
         * 业务仿重ID - 外部透传。返利、行为等唯一标识
         */
        private String outBusinessNo;
        private TradeNameVO tradeNameVO;
    }

}
