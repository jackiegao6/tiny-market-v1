package com.gzc.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.gzc.domain.activity.model.entity.SkuRechargeEntity;
import com.gzc.domain.activity.service.IRaffleQuotaService;
import com.gzc.domain.credit.model.entity.CreditTradeEntity;
import com.gzc.domain.credit.model.valobj.TradeNameVO;
import com.gzc.domain.credit.model.valobj.TradeTypeVO;
import com.gzc.domain.credit.service.ICreditAdjustService;
import com.gzc.domain.rebate.event.SendRebateMessageEvent;
import com.gzc.types.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class RebateMessageCustomer {

    @Value("${spring.rabbitmq.topic.send_rebate}")
    private String topic;

    private final IRaffleQuotaService raffleQuotaService;
    private final ICreditAdjustService creditAdjustService;

    /**
     * 接收返利消息
     *
     * 两种情况
     *      给用户增加某sku的抽奖次数
     *      给用户增加积分
     * @param message
     */
    @RabbitListener(queuesToDeclare = @Queue(value = "${spring.rabbitmq.topic.send_rebate}"))
    public void listener(String message){

        try{
            BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage>>() {
            }.getType());

            SendRebateMessageEvent.RebateMessage msgBody = eventMessage.getData();

            switch (msgBody.getRebateType()){
                case "sku":
                    SkuRechargeEntity skuRechargeEntity = SkuRechargeEntity.builder()
                            .userId(msgBody.getUserId())
                            .sku(Long.valueOf(msgBody.getRebateConfig()))
                            .outBusinessNo(msgBody.getBizId())
                            .build();
                    raffleQuotaService.createSkuRechargeOrder(skuRechargeEntity);
                    break;
                case "integral":
                    CreditTradeEntity creditTradeEntity = CreditTradeEntity.builder()
                            .userId(msgBody.getUserId())
                            .tradeName(TradeNameVO.REBATE)
                            .tradeType(TradeTypeVO.FORWARD)
                            .amount(new BigDecimal(msgBody.getRebateConfig()))
                            .outBusinessNo(msgBody.getBizId())
                            .build();
                    creditAdjustService.createCreditOrderService(creditTradeEntity);
                    break;
            }
        }catch (Exception e){
            log.error("用户充值行为信息 消费失败 topic:{}", topic, e);
            throw e;
        }

    }

}
