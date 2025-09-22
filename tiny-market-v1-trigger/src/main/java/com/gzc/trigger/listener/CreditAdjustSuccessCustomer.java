package com.gzc.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.gzc.domain.activity.model.entity.DeliveryOrderEntity;
import com.gzc.domain.activity.service.IRaffleQuotaService;
import com.gzc.domain.credit.event.CreditAdjustSuccessMessageEvent;
import com.gzc.domain.credit.model.valobj.TradeNameVO;
import com.gzc.types.enums.ResponseCode;
import com.gzc.types.event.BaseEvent;
import com.gzc.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description 积分调整成功消息
 */
@Slf4j
@Component
public class CreditAdjustSuccessCustomer {

    @Value("${spring.rabbitmq.topic.credit_adjust_success}")
    private String topic;
    @Resource
    private IRaffleQuotaService raffleQuotaService;

    @RabbitListener(queuesToDeclare = @Queue(value = "${spring.rabbitmq.topic.credit_adjust_success}"))
    public void listener(String message) {
        try {
            BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustMessage> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustMessage>>() {
            }.getType());
            CreditAdjustSuccessMessageEvent.CreditAdjustMessage creditAdjustSuccessMessage = eventMessage.getData();

            // 至此 积分调整已经完成
            // 目前有两种情况 一种是日常返利导致的积分充值
            // 一种是积分购买次数导致的积分减少
            // 后续支持 外部支付导致的积分充值

            // 如果是第二种就需要导致次数变更
            if (TradeNameVO.CONVERT_SKU.getName().equals(creditAdjustSuccessMessage.getTradeNameVO().getName())){

                DeliveryOrderEntity deliveryOrderEntity = new DeliveryOrderEntity();
                deliveryOrderEntity.setUserId(creditAdjustSuccessMessage.getUserId());
                deliveryOrderEntity.setOutBusinessNo(creditAdjustSuccessMessage.getOutBusinessNo());
                raffleQuotaService.updateOrder(deliveryOrderEntity);
            }
        } catch (AppException e) {
            if (ResponseCode.INDEX_DUP.getCode().equals(e.getCode())) {
                log.warn("监听积分账户调整成功消息，进行交易商品发货，消费重复 topic: {} message: {}", topic, message, e);
                return;
            }
            throw e;
        } catch (Exception e) {
            log.error("监听积分账户调整成功消息，进行交易商品发货失败 topic: {} message: {}", topic, message, e);
            throw e;
        }
    }

}
