package com.gzc.infrastructure.adapter.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.alibaba.fastjson2.JSON;
import com.gzc.domain.award.adapter.repository.IAwardRepository;
import com.gzc.domain.award.model.aggregate.UserAwardRecordAggregate;
import com.gzc.domain.award.model.entity.TaskEntity;
import com.gzc.domain.award.model.entity.UserAwardRecordEntity;
import com.gzc.infrastructure.dao.ITaskDao;
import com.gzc.infrastructure.dao.IUserAwardRecordDao;
import com.gzc.infrastructure.dao.IUserRaffleOrderDao;
import com.gzc.infrastructure.dao.po.Task;
import com.gzc.infrastructure.dao.po.UserAwardRecord;
import com.gzc.infrastructure.dao.po.UserRaffleOrder;
import com.gzc.infrastructure.event.EventPublisher;
import com.gzc.types.enums.ResponseCode;
import com.gzc.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

@Repository
@Slf4j
@RequiredArgsConstructor
public class AwardRepository implements IAwardRepository {


    private final ITaskDao taskDao;
    private final IUserAwardRecordDao userAwardRecordDao;
    private final IDBRouterStrategy dbRouter;
    private final TransactionTemplate transactionTemplate;
    private final EventPublisher eventPublisher;
    private final IUserRaffleOrderDao userRaffleOrderDao;

    @Override
    public void saveUserAwardRecord(UserAwardRecordAggregate aggregate) {
        UserAwardRecordEntity userAwardRecordEntity = aggregate.getUserAwardRecordEntity();
        TaskEntity taskEntity = aggregate.getTaskEntity();

        UserAwardRecord record = UserAwardRecord.builder()
                .userId(userAwardRecordEntity.getUserId())
                .activityId(userAwardRecordEntity.getActivityId())
                .strategyId(userAwardRecordEntity.getStrategyId())
                .orderId(userAwardRecordEntity.getOrderId())
                .awardId(userAwardRecordEntity.getAwardId())
                .awardTitle(userAwardRecordEntity.getAwardTitle())
                .awardTime(userAwardRecordEntity.getAwardTime())
                .awardState(userAwardRecordEntity.getAwardState().getCode())
                .build();

        Task task = Task.builder()
                .userId(taskEntity.getUserId())
                .topic(taskEntity.getTopic())
                .messageId(taskEntity.getMessageId())
                .message(JSON.toJSONString(taskEntity.getMessage()))
                .state(taskEntity.getState().getCode())
                .build();

        UserRaffleOrder userRaffleOrder = new UserRaffleOrder();
        userRaffleOrder.setUserId(userAwardRecordEntity.getUserId());
        userRaffleOrder.setOrderId(userAwardRecordEntity.getOrderId());

        try {
            dbRouter.doRouter(userAwardRecordEntity.getUserId());
            transactionTemplate.execute(status -> {
                try {
                    userAwardRecordDao.insert(record);
                    taskDao.insert(task);
                    int res = userRaffleOrderDao.updateUserRaffleOrderStateUsed(userRaffleOrder);
                    if (res != 1){
                        status.setRollbackOnly();
                        throw new AppException(ResponseCode.ACTIVITY_ORDER_ERROR.getCode(), ResponseCode.ACTIVITY_ORDER_ERROR.getInfo());
                    }
                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                }
            });

        }
        finally {
            dbRouter.clear();
        }

        try{
            // MQ 消息不是数据库事务。所以需要写入一个 task 表，通过任务补偿的方式进行处理
            eventPublisher.publish(task.getTopic(), task.getMessage());
            taskDao.updateTaskSendMessageCompleted(task);
        }catch (Exception e){
            taskDao.updateTaskSendMessageFail(task);
        }
    }
}
