package com.gzc.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.gzc.domain.activity.model.entity.SkuRechargeEntity;
import com.gzc.domain.activity.service.IRaffleQuota;
import com.gzc.domain.rebate.event.SendRebateMessageEvent;
import com.gzc.domain.rebate.model.valobj.RebateTypeVO;
import com.gzc.types.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RebateMessageCustomer {

    @Value("${spring.rabbitmq.topic.send_rebate}")
    private String topic;

    private final IRaffleQuota raffleQuota;

    @RabbitListener(queuesToDeclare = @Queue(value = "${spring.rabbitmq.topic.send_rebate}"))
    public void listener(String message){

        try{
            BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage>>() {
            }.getType());

            SendRebateMessageEvent.RebateMessage msgBody = eventMessage.getData();
            if (!RebateTypeVO.SKU.getCode().equals(msgBody.getRebateType())){
                // todo
                return;
            }
            SkuRechargeEntity skuRechargeEntity = SkuRechargeEntity.builder()
                        .userId(msgBody.getUserId())
                        .sku(Long.valueOf(msgBody.getRebateConfig()))
                        .outBusinessNo(msgBody.getBizId())
                        .build();

            raffleQuota.createSkuRechargeOrder(skuRechargeEntity);

        }catch (Exception e){
            log.error("用户充值行为信息 消费失败 topic:{}", topic, e);
            throw e;
        }

    }

}
