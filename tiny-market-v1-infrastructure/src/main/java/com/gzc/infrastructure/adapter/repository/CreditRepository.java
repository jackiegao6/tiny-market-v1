package com.gzc.infrastructure.adapter.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.alibaba.fastjson.JSON;
import com.gzc.domain.award.model.valobj.AccountStatusVO;
import com.gzc.domain.credit.adapter.repository.ICreditRepository;
import com.gzc.domain.credit.model.aggregate.TradeAggregate;
import com.gzc.domain.credit.model.entity.CreditAccountEntity;
import com.gzc.domain.credit.model.entity.CreditAdjustTaskEntity;
import com.gzc.domain.credit.model.entity.CreditOrderEntity;
import com.gzc.domain.credit.model.valobj.TradeTypeVO;
import com.gzc.infrastructure.dao.ITaskDao;
import com.gzc.infrastructure.dao.IUserCreditAccountDao;
import com.gzc.infrastructure.dao.IUserCreditOrderDao;
import com.gzc.infrastructure.dao.po.Task;
import com.gzc.infrastructure.dao.po.UserCreditAccount;
import com.gzc.infrastructure.dao.po.UserCreditOrder;
import com.gzc.infrastructure.event.EventPublisher;
import com.gzc.infrastructure.redis.IRedisService;
import com.gzc.types.common.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CreditRepository implements ICreditRepository {

    private final IRedisService redisService;
    private final IUserCreditAccountDao userCreditAccountDao;
    private final IUserCreditOrderDao userCreditOrderDao;
    private final IDBRouterStrategy dbRouter;
    private final TransactionTemplate transactionTemplate;
    private final ITaskDao taskDao;
    private final EventPublisher eventPublisher;

    @Override
    public void adjustCreditAccount(TradeAggregate tradeAggregate) {
        String userId = tradeAggregate.getUserId();
        CreditAccountEntity creditAccountEntity = tradeAggregate.getCreditAccountEntity();
        CreditOrderEntity creditOrderEntity = tradeAggregate.getCreditOrderEntity();
        CreditAdjustTaskEntity creditAdjustTaskEntity = tradeAggregate.getCreditAdjustTaskEntity();

        // 积分账户
        UserCreditAccount userCreditAccountReq = new UserCreditAccount();
        userCreditAccountReq.setUserId(userId);
        userCreditAccountReq.setTotalAmount(creditAccountEntity.getAdjustAmount());
        userCreditAccountReq.setAvailableAmount(creditAccountEntity.getAdjustAmount());
        userCreditAccountReq.setAccountStatus(AccountStatusVO.OPEN.getCode());

        // 2. 新增 积分增加订单
        UserCreditOrder userCreditOrderReq = new UserCreditOrder();
        userCreditOrderReq.setUserId(creditOrderEntity.getUserId());
        userCreditOrderReq.setOrderId(creditOrderEntity.getOrderId());
        userCreditOrderReq.setTradeName(creditOrderEntity.getTradeName().getName());
        userCreditOrderReq.setTradeType(creditOrderEntity.getTradeType().getCode());
        userCreditOrderReq.setTradeAmount(creditOrderEntity.getTradeAmount());
        userCreditOrderReq.setOutBusinessNo(creditOrderEntity.getOutBusinessNo());

        // 3. 新增 积分增加任务 状态: create
        Task task = new Task();
        task.setUserId(creditAdjustTaskEntity.getUserId());
        task.setTopic(creditAdjustTaskEntity.getTopic());
        task.setMessageId(creditAdjustTaskEntity.getMessageId());
        task.setMessage(JSON.toJSONString(creditAdjustTaskEntity.getMessage()));
        task.setState(creditAdjustTaskEntity.getState().getCode());

        RLock lock = redisService.getLock(Constants.RedisKey.USER_CREDIT_ACCOUNT_LOCK + userId + Constants.UNDERLINE + creditOrderEntity.getOutBusinessNo());
        try {
            lock.lock(3, TimeUnit.SECONDS);
            dbRouter.doRouter(userId);
            // 编程式事务
            transactionTemplate.execute(status -> {
                try {
                    // 1. 保存账户积分
                    UserCreditAccount userCreditAccount = userCreditAccountDao.queryUserCreditAccount(userCreditAccountReq);
                    if (null == userCreditAccount) {
                        userCreditAccountDao.insert(userCreditAccountReq);
                    } else {
                        if(TradeTypeVO.FORWARD == creditOrderEntity.getTradeType()){
                            userCreditAccountDao.updateAddAmount(userCreditAccountReq);
                        }else {
                            userCreditAccountDao.updateSubtractionAmount(userCreditAccountReq);
                        }
                    }
                    // 2. 保存账户订单
                    userCreditOrderDao.insert(userCreditOrderReq);
                    // 3. 写入任务
                    taskDao.insert(task);
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("调整账户积分额度异常，唯一索引冲突 userId:{} orderId:{}", userId, creditOrderEntity.getOrderId(), e);
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("调整账户积分额度失败 userId:{} orderId:{}", userId, creditOrderEntity.getOrderId(), e);
                }
                return 1;
            });
        } finally {
            dbRouter.clear();
            lock.unlock();
        }

        // 至此 积分账户调整完成
        try {
            // 发送消息【在事务外执行，如果失败还有任务补偿】
            eventPublisher.publish(task.getTopic(), task.getMessage());
            // 更新数据库记录，task 任务表
            taskDao.updateTaskSendMessageCompleted(task);
            log.info("调整账户积分记录，发送MQ消息完成 userId: {} orderId:{} topic: {}", userId, creditOrderEntity.getOrderId(), task.getTopic());
        } catch (Exception e) {
            log.error("调整账户积分记录，发送MQ消息失败 userId: {} topic: {}", userId, task.getTopic());
            taskDao.updateTaskSendMessageFail(task);
        }
    }

    @Override
    public CreditAccountEntity queryUserCreditAccount(String userId) {
        UserCreditAccount userCreditAccountReq = new UserCreditAccount();
        userCreditAccountReq.setUserId(userId);
        try {
            dbRouter.doRouter(userId);
            UserCreditAccount userCreditAccount = userCreditAccountDao.queryUserCreditAccount(userCreditAccountReq);
            return CreditAccountEntity.builder().userId(userId).adjustAmount(userCreditAccount.getAvailableAmount()).build();
        } finally {
            dbRouter.clear();
        }

    }
}
