package com.gzc.domain.award.service;

import com.gzc.domain.award.adapter.repository.IAwardRepository;
import com.gzc.domain.award.event.SendAwardMessageEvent;
import com.gzc.domain.award.model.aggregate.UserAwardRecordAggregate;
import com.gzc.domain.award.model.entity.DistributeAwardEntity;
import com.gzc.domain.award.model.entity.TaskEntity;
import com.gzc.domain.award.model.entity.UserAwardRecordEntity;
import com.gzc.domain.award.service.distribute.IDistributeAwardService;
import com.gzc.types.enums.MQTaskStateVO;
import com.gzc.types.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AwardService implements IAwardService {

    private final IAwardRepository awardRepository;
    private final SendAwardMessageEvent sendAwardMessageEvent;
    private final Map<String, IDistributeAwardService> distributeAwardMap;

    @Override
    public void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity) {
        SendAwardMessageEvent.SendAwardMessage messageBody = SendAwardMessageEvent.SendAwardMessage.builder()
                                    .userId(userAwardRecordEntity.getUserId())
                                    .orderId(userAwardRecordEntity.getOrderId())
                                    .awardId(userAwardRecordEntity.getAwardId())
                                    .awardTitle(userAwardRecordEntity.getAwardTitle())
                                    .awardConfig(userAwardRecordEntity.getAwardConfig())
                                    .build();
        BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> message = sendAwardMessageEvent.buildEventMessage(messageBody);

        TaskEntity taskEntity = TaskEntity.builder()
                .userId(userAwardRecordEntity.getUserId())
                .topic(sendAwardMessageEvent.topic())
                .messageId(message.getId())
                .message(message)
                .state(MQTaskStateVO.create)
                .build();

        UserAwardRecordAggregate aggregate = UserAwardRecordAggregate.builder()
                .taskEntity(taskEntity)
                .userAwardRecordEntity(userAwardRecordEntity)
                .build();

        awardRepository.saveUserAwardRecord(aggregate);
    }

    @Override
    public void distributeAward(DistributeAwardEntity distributeAwardEntity) {
        String awardKey = awardRepository.queryAwardKey(distributeAwardEntity.getAwardId());
        if (awardKey == null) {
            log.error("分发奖品不存在");
            return;
        }

        IDistributeAwardService distributeAwardService = distributeAwardMap.get(awardKey);
        distributeAwardService.giveOutPrizes(distributeAwardEntity);

    }
}
