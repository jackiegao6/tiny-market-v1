package com.gzc.domain.award.service;

import com.gzc.domain.award.adapter.repository.IAwardRepository;
import com.gzc.domain.award.event.SendAwardMessageEvent;
import com.gzc.domain.award.model.aggregate.UserAwardRecordAggregate;
import com.gzc.domain.award.model.entity.TaskEntity;
import com.gzc.domain.award.model.entity.UserAwardRecordEntity;
import com.gzc.domain.award.model.valobj.TaskStateVO;
import com.gzc.types.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AwardService implements IAwardService{

    private final IAwardRepository repository;
    private final SendAwardMessageEvent sendAwardMessageEvent;

    @Override
    public void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity) {
        SendAwardMessageEvent.SendAwardMessage messageBody = SendAwardMessageEvent.SendAwardMessage.builder()
                                    .userId(userAwardRecordEntity.getUserId())
                                    .awardId(userAwardRecordEntity.getAwardId())
                                    .awardTitle(userAwardRecordEntity.getAwardTitle())
                                    .build();
        BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> message = sendAwardMessageEvent.buildEventMessage(messageBody);

        TaskEntity taskEntity = TaskEntity.builder()
                .userId(userAwardRecordEntity.getUserId())
                .topic(sendAwardMessageEvent.topic())
                .messageId(message.getId())
                .message(message)
                .state(TaskStateVO.create)
                .build();

        UserAwardRecordAggregate aggregate = UserAwardRecordAggregate.builder()
                .taskEntity(taskEntity)
                .userAwardRecordEntity(userAwardRecordEntity)
                .build();

        repository.saveUserAwardRecord(aggregate);


    }
}
