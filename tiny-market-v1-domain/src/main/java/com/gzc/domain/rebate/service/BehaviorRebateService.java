package com.gzc.domain.rebate.service;

import com.gzc.domain.rebate.event.SendRebateMessageEvent;
import com.gzc.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import com.gzc.domain.rebate.model.entity.BehaviorEntity;
import com.gzc.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import com.gzc.domain.rebate.model.entity.BehaviorRebateTaskEntity;
import com.gzc.domain.rebate.model.valobj.DailyBehaviorRebateVO;
import com.gzc.domain.rebate.repository.IBehaviorRebateRepository;
import com.gzc.types.common.Constants;
import com.gzc.types.enums.MQTaskStateVO;
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
public class BehaviorRebateService implements IBehaviorRebateService {

    private final IBehaviorRebateRepository behaviorRebateRepository;
    private final SendRebateMessageEvent sendRebateMessageEvent;

    @Override
    public List<String> createRebateOrder(BehaviorEntity behaviorEntity) {
        String userId = behaviorEntity.getUserId();
        String outBusinessNo = behaviorEntity.getOutBusinessNo();

        // 1. 查询签到的返利配置列表
        List<DailyBehaviorRebateVO> rebateConfigList = behaviorRebateRepository.queryDailyBehaviorRebateConfig(behaviorEntity.getBehaviorVO());
        if (null == rebateConfigList || rebateConfigList.isEmpty()) return new ArrayList<>();

        // 2. 构建聚合对象
        List<String> rebateOrderIds = new ArrayList<>();
        List<BehaviorRebateAggregate> behaviorRebateAggregates = new ArrayList<>();

        for (DailyBehaviorRebateVO rebateConfigVO : rebateConfigList) {
            // bizId: gzc_sign_2025-09-22
            String bizId = userId + Constants.UNDERLINE + rebateConfigVO.getRebateType() + Constants.UNDERLINE + outBusinessNo;

            // todo 雪花算法
            String rebateOrderId = RandomStringUtils.randomNumeric(12);
            BehaviorRebateOrderEntity rebateOrderEntity = BehaviorRebateOrderEntity.builder()
                    .userId(userId)
                    .orderId(rebateOrderId)
                    .behaviorType(rebateConfigVO.getBehaviorType())
                    .rebateDesc(rebateConfigVO.getRebateDesc())
                    .rebateType(rebateConfigVO.getRebateType())
                    .rebateConfig(rebateConfigVO.getRebateConfig())
                    .outBusinessNo(outBusinessNo)
                    .bizId(bizId)
                    .build();

            rebateOrderIds.add(rebateOrderId);

            // MQ + 任务
            // 把返利的配置放入消息中
            SendRebateMessageEvent.RebateMessage messageBody = SendRebateMessageEvent.RebateMessage.builder()
                    .userId(userId)
                    .rebateType(rebateConfigVO.getRebateType())
                    .rebateConfig(rebateConfigVO.getRebateConfig())
                    .bizId(bizId)
                    .build();
            BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> rebateMessageEventMessage = sendRebateMessageEvent.buildEventMessage(messageBody);

            BehaviorRebateTaskEntity behaviorRebateTaskEntity = BehaviorRebateTaskEntity.builder()
                    .userId(userId)
                    .topic(sendRebateMessageEvent.topic())
                    .messageId(rebateMessageEventMessage.getId())
                    .message(rebateMessageEventMessage)
                    .taskStateVO(MQTaskStateVO.create)
                    .build();


            BehaviorRebateAggregate behaviorRebateAggregate = BehaviorRebateAggregate.builder()
                    .behaviorRebateOrderEntity(rebateOrderEntity)
                    .behaviorRebateTaskEntity(behaviorRebateTaskEntity)
                    .build();

            behaviorRebateAggregates.add(behaviorRebateAggregate);
        }

        // 3. 存储聚合对象数据
        behaviorRebateRepository.saveUserRebateRecord(userId, behaviorRebateAggregates);

        return rebateOrderIds;
    }


    @Override
    public List<BehaviorRebateOrderEntity> queryOrderByOutBusinessNo(String userId, String outBusinessNo) {
        return behaviorRebateRepository.queryOrderByOutBusinessNo(userId, outBusinessNo);
    }
}
