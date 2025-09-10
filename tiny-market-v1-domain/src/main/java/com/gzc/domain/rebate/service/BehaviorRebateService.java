package com.gzc.domain.rebate.service;

import com.gzc.domain.rebate.event.SendRebateMessageEvent;
import com.gzc.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import com.gzc.domain.rebate.model.entity.BehaviorEntity;
import com.gzc.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import com.gzc.domain.rebate.model.entity.BehaviorRebateTaskEntity;
import com.gzc.domain.rebate.model.valobj.DailyBehaviorRebateVO;
import com.gzc.domain.rebate.model.valobj.TaskStateVO;
import com.gzc.domain.rebate.repository.IBehaviorRebateRepository;
import com.gzc.types.common.Constants;
import com.gzc.types.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class BehaviorRebateService implements IBehaviorRebateService{

    private final IBehaviorRebateRepository behaviorRebateRepository;
    private final SendRebateMessageEvent sendRebateMessageEvent;

    @Override
    public List<String> createRebateOrder(BehaviorEntity behaviorEntity) {
        String userId = behaviorEntity.getUserId();

        // 1. 查询返利配置
        List<DailyBehaviorRebateVO> dailyBehaviorRebateVOS = behaviorRebateRepository.queryDailyBehaviorRebateConfig(behaviorEntity.getBehaviorVO());
        if (null == dailyBehaviorRebateVOS || dailyBehaviorRebateVOS.isEmpty()){
            return new ArrayList<>();
        }

        // 2. 构建聚合对象
        List<String> orderIds = new ArrayList<>();
        List<BehaviorRebateAggregate> behaviorRebateAggregates = new ArrayList<>();
        for (DailyBehaviorRebateVO dailyBehaviorRebateVO : dailyBehaviorRebateVOS) {
            String bizId = userId + Constants.UNDERLINE + dailyBehaviorRebateVO.getRebateType() + Constants.UNDERLINE + behaviorEntity.getOutBusinessNo();
            String orderId = RandomStringUtils.randomNumeric(12);
            BehaviorRebateOrderEntity behaviorRebateOrderEntity = BehaviorRebateOrderEntity.builder()
                        .userId(userId)
                        .orderId(orderId)
                        .behaviorType(dailyBehaviorRebateVO.getBehaviorType())
                        .rebateDesc(dailyBehaviorRebateVO.getRebateDesc())
                        .rebateType(dailyBehaviorRebateVO.getRebateType())
                        .rebateConfig(dailyBehaviorRebateVO.getRebateConfig())
                        .bizId(bizId)
                        .build();
            orderIds.add(orderId);

            // MQ + 任务
            SendRebateMessageEvent.RebateMessage messageBody = SendRebateMessageEvent.RebateMessage.builder()
                    .userId(userId)
                    .rebateType(dailyBehaviorRebateVO.getRebateType())
                    .rebateConfig(dailyBehaviorRebateVO.getRebateConfig())
                    .bizId(bizId)
                    .build();
            BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> rebateMessageEventMessage = sendRebateMessageEvent.buildEventMessage(messageBody);

            BehaviorRebateTaskEntity behaviorRebateTaskEntity = BehaviorRebateTaskEntity.builder()
                        .userId(userId)
                        .topic(sendRebateMessageEvent.topic())
                        .messageId(rebateMessageEventMessage.getId())
                        .message(rebateMessageEventMessage)
                        .taskStateVO(TaskStateVO.CREATE)
                        .build();


            BehaviorRebateAggregate behaviorRebateAggregate = BehaviorRebateAggregate.builder()
                        .behaviorRebateOrderEntity(behaviorRebateOrderEntity)
                        .behaviorRebateTaskEntity(behaviorRebateTaskEntity)
                        .build();

            behaviorRebateAggregates.add(behaviorRebateAggregate);
        }

        // 3. 存储聚合对象数据
        behaviorRebateRepository.saveUserRebateRecord(userId, behaviorRebateAggregates);

        return orderIds;
    }
}
