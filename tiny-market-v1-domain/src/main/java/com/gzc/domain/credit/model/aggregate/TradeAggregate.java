package com.gzc.domain.credit.model.aggregate;

import com.gzc.domain.credit.event.CreditAdjustSuccessMessageEvent;
import com.gzc.domain.credit.model.entity.CreditAccountEntity;
import com.gzc.domain.credit.model.entity.CreditOrderEntity;
import com.gzc.domain.credit.model.entity.CreditAdjustTaskEntity;
import com.gzc.domain.credit.model.valobj.TradeNameVO;
import com.gzc.domain.credit.model.valobj.TradeTypeVO;
import com.gzc.types.enums.MQTaskStateVO;
import com.gzc.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;

/**
 * @description 交易聚合对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeAggregate {

    // 用户ID
    private String userId;
    // 积分账户实体
    private CreditAccountEntity creditAccountEntity;
    // 积分订单实体
    private CreditOrderEntity creditOrderEntity;
    private CreditAdjustTaskEntity creditAdjustTaskEntity;

    public static CreditAccountEntity createCreditAccountEntity(String userId, BigDecimal adjustAmount) {
        return CreditAccountEntity.builder().userId(userId).adjustAmount(adjustAmount).build();
    }

    public static CreditOrderEntity createCreditOrderEntity(String userId,
                                                            TradeNameVO tradeName,
                                                            TradeTypeVO tradeType,
                                                            BigDecimal tradeAmount,
                                                            String outBusinessNo) {
        return CreditOrderEntity.builder()
                .userId(userId)
                .orderId(RandomStringUtils.randomNumeric(12))
                .tradeName(tradeName)
                .tradeType(tradeType)
                .tradeAmount(tradeAmount)
                .outBusinessNo(outBusinessNo)
                .build();
    }

    public static CreditAdjustTaskEntity createCreditAdjustTaskEntity(String userId, String topic, String messageId, BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage> message) {
        CreditAdjustTaskEntity taskEntity = new CreditAdjustTaskEntity();
        taskEntity.setUserId(userId);
        taskEntity.setTopic(topic);
        taskEntity.setMessageId(messageId);
        taskEntity.setMessage(message);
        taskEntity.setState(MQTaskStateVO.create);
        return taskEntity;
    }

}
