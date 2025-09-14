package com.gzc.infrastructure.adapter.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.alibaba.fastjson2.JSON;
import com.gzc.domain.award.adapter.repository.IAwardRepository;
import com.gzc.domain.award.model.aggregate.GiveOutPrizesAggregate;
import com.gzc.domain.award.model.aggregate.UserAwardRecordAggregate;
import com.gzc.domain.award.model.entity.TaskEntity;
import com.gzc.domain.award.model.entity.UserAwardRecordEntity;
import com.gzc.domain.award.model.entity.UserCreditAwardEntity;
import com.gzc.domain.award.model.valobj.AccountStatusVO;
import com.gzc.infrastructure.dao.*;
import com.gzc.infrastructure.dao.po.Task;
import com.gzc.infrastructure.dao.po.UserAwardRecord;
import com.gzc.infrastructure.dao.po.UserCreditAccount;
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

    private final IAwardDao awardDao;
    private final ITaskDao taskDao;
    private final IUserAwardRecordDao userAwardRecordDao;
    private final IDBRouterStrategy dbRouter;
    private final TransactionTemplate transactionTemplate;
    private final EventPublisher eventPublisher;
    private final IUserRaffleOrderDao userRaffleOrderDao;
    private final IUserCreditAccountDao userCreditAccountDao;

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
                    // 写入用户中奖记录
                    userAwardRecordDao.insert(record);
                    taskDao.insert(task);
                    // 至此用户抽奖记录状态为used
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


    @Override
    public String queryAwardConfig(Integer awardId) {
        return awardDao.queryAwardConfigByAwardId(awardId);
    }

    @Override
    public void saveGiveOutPrizesAggregate(GiveOutPrizesAggregate giveOutPrizesAggregate) {
        String userId = giveOutPrizesAggregate.getUserId();
        UserCreditAwardEntity userCreditAwardEntity = giveOutPrizesAggregate.getUserCreditAwardEntity();
        UserAwardRecordEntity userAwardRecordEntity = giveOutPrizesAggregate.getUserAwardRecordEntity();

        // 更新发奖记录
        UserAwardRecord userAwardRecordReq = new UserAwardRecord();
        userAwardRecordReq.setUserId(userId);
        userAwardRecordReq.setOrderId(userAwardRecordEntity.getOrderId());
        userAwardRecordReq.setAwardState(userAwardRecordEntity.getAwardState().getCode());

        // 更新用户积分 「首次则插入数据」
        UserCreditAccount userCreditAccountReq = new UserCreditAccount();
        userCreditAccountReq.setUserId(userCreditAwardEntity.getUserId());
        userCreditAccountReq.setTotalAmount(userCreditAwardEntity.getCreditAmount());
        userCreditAccountReq.setAvailableAmount(userCreditAwardEntity.getCreditAmount());
        userCreditAccountReq.setAccountStatus(AccountStatusVO.OPEN.getCode());

        try {
            dbRouter.doRouter(giveOutPrizesAggregate.getUserId());
            transactionTemplate.execute(status -> {
                try {
                    // 更新积分 || 创建积分账户
                    int updateAccountCount = userCreditAccountDao.updateAddAmount(userCreditAccountReq);
                    if (0 == updateAccountCount) {
                        userCreditAccountDao.insert(userCreditAccountReq);
                    }

                    // 更新奖品记录
                    int updateAwardCount = userAwardRecordDao.updateAwardRecordCompletedState(userAwardRecordReq);
                    if (0 == updateAwardCount) {
                        log.warn("更新中奖记录，重复更新拦截 userId:{} giveOutPrizesAggregate:{}", userId, com.alibaba.fastjson.JSON.toJSONString(giveOutPrizesAggregate));
                        status.setRollbackOnly();
                    }
                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("更新中奖记录，唯一索引冲突 userId: {} ", userId, e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                }
            });
        } finally {
            dbRouter.clear();
        }
    }

    @Override
    public String queryAwardKey(Integer awardId) {
        return awardDao.queryAwardKeyByAwardId(awardId);
    }
}
