package com.gzc.infrastructure.event;

import com.alibaba.fastjson.JSON;
import com.gzc.types.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description 消息发送
 */
@Slf4j
@Component
public class EventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publish(String topic, BaseEvent.EventMessage<?> eventMessage) {
        try {
            String messageJson = JSON.toJSONString(eventMessage);
            rabbitTemplate.convertAndSend(topic, messageJson);
            log.info("发送MQ消息 topic:{} message:{}", topic, messageJson);
        } catch (Exception e) {
            log.error("发送MQ消息失败 topic:{} message:{}", topic, JSON.toJSONString(eventMessage), e);
            throw e;
        }
    }

    public void publish(String topic, String jsonBody){
        try {
            rabbitTemplate.convertAndSend(topic, jsonBody);
        } catch (AmqpException e) {
            log.error("发送MQ消息失败 topic:{} message:{}", topic, jsonBody);
            throw new RuntimeException(e);
        }
    }

}
