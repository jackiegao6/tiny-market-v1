package com.gzc.trigger.listener;

import com.gzc.domain.award.service.IAwardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description 用户奖品记录消息消费者
 */
@Slf4j
@Component
public class SendAwardCustomer {

    @Value("${spring.rabbitmq.topic.send_award}")
    private String topic;
    @Resource
    private IAwardService awardService;


    @RabbitListener(queuesToDeclare = @Queue(value = "${spring.rabbitmq.topic.send_award}"))
    public void listener(String message) {
        try {
            // todo 发放奖品

//            BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage>>() {
//            }.getType());
//            SendAwardMessageEvent.SendAwardMessage sendAwardMessage = eventMessage.getData();
//
//            // 发放奖品
//            DistributeAwardEntity distributeAwardEntity = new DistributeAwardEntity();
//            distributeAwardEntity.setUserId(sendAwardMessage.getUserId());
//            distributeAwardEntity.setOrderId(sendAwardMessage.getOrderId());
//            distributeAwardEntity.setAwardId(sendAwardMessage.getAwardId());
//            distributeAwardEntity.setAwardConfig(sendAwardMessage.getAwardConfig());
//            awardService.distributeAward(distributeAwardEntity);
        } catch (Exception e) {
            log.error("监听用户奖品发送消息，消费失败 topic: {} message: {}", topic, message);
            throw e;
        }
    }

}
