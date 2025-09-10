package com.gzc.infrastructure.adapter.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.alibaba.fastjson.JSON;
import com.gzc.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import com.gzc.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import com.gzc.domain.rebate.model.entity.BehaviorRebateTaskEntity;
import com.gzc.domain.rebate.model.valobj.BehaviorVO;
import com.gzc.domain.rebate.model.valobj.DailyBehaviorRebateVO;
import com.gzc.domain.rebate.repository.IBehaviorRebateRepository;
import com.gzc.infrastructure.dao.IDailyBehaviorRebateDao;
import com.gzc.infrastructure.dao.ITaskDao;
import com.gzc.infrastructure.dao.IUserBehaviorRebateOrderDao;
import com.gzc.infrastructure.dao.po.DailyBehaviorRebate;
import com.gzc.infrastructure.dao.po.Task;
import com.gzc.infrastructure.dao.po.UserBehaviorRebateOrder;
import com.gzc.infrastructure.event.EventPublisher;
import com.gzc.types.enums.ResponseCode;
import com.gzc.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class RebateRepository implements IBehaviorRebateRepository {

    private final IDBRouterStrategy dbRouter;
    private final TransactionTemplate transactionTemplate;
    private final IUserBehaviorRebateOrderDao userBehaviorRebateOrderDao;
    private final IDailyBehaviorRebateDao dailyBehaviorRebateDao;
    private final ITaskDao taskDao;
    private final EventPublisher eventPublisher;


    @Override
    public List<DailyBehaviorRebateVO> queryDailyBehaviorRebateConfig(BehaviorVO behaviorVO) {
        List<DailyBehaviorRebate> dailyBehaviorRebates = dailyBehaviorRebateDao.queryDailyBehaviorRebateByBehaviorType(behaviorVO.getCode());

        List<DailyBehaviorRebateVO> dailyBehaviorRebateVOS = new ArrayList<>(dailyBehaviorRebates.size());
        for (DailyBehaviorRebate dailyBehaviorRebate : dailyBehaviorRebates) {
            dailyBehaviorRebateVOS.add(DailyBehaviorRebateVO.builder()
                    .behaviorType(dailyBehaviorRebate.getBehaviorType())
                    .rebateDesc(dailyBehaviorRebate.getRebateDesc())
                    .rebateType(dailyBehaviorRebate.getRebateType())
                    .rebateConfig(dailyBehaviorRebate.getRebateConfig())
                    .build());
        }
        return dailyBehaviorRebateVOS;
    }

    @Override
    public void saveUserRebateRecord(String userId, List<BehaviorRebateAggregate> behaviorRebateAggregates) {
        try {
            dbRouter.doRouter(userId);
            transactionTemplate.execute(status -> {
                try{
                    for (BehaviorRebateAggregate behaviorRebateAggregate : behaviorRebateAggregates) {
                        BehaviorRebateOrderEntity behaviorRebateOrderEntity = behaviorRebateAggregate.getBehaviorRebateOrderEntity();
                        // 用户行为返利订单对象
                        UserBehaviorRebateOrder userBehaviorRebateOrder = new UserBehaviorRebateOrder();
                        userBehaviorRebateOrder.setUserId(behaviorRebateOrderEntity.getUserId());
                        userBehaviorRebateOrder.setOrderId(behaviorRebateOrderEntity.getOrderId());
                        userBehaviorRebateOrder.setBehaviorType(behaviorRebateOrderEntity.getBehaviorType());
                        userBehaviorRebateOrder.setRebateDesc(behaviorRebateOrderEntity.getRebateDesc());
                        userBehaviorRebateOrder.setRebateType(behaviorRebateOrderEntity.getRebateType());
                        userBehaviorRebateOrder.setRebateConfig(behaviorRebateOrderEntity.getRebateConfig());
                        userBehaviorRebateOrder.setBizId(behaviorRebateOrderEntity.getBizId());
                        userBehaviorRebateOrderDao.insert(userBehaviorRebateOrder);

                        // 任务对象
                        BehaviorRebateTaskEntity taskEntity = behaviorRebateAggregate.getBehaviorRebateTaskEntity();
                        Task task = new Task();
                        task.setUserId(taskEntity.getUserId());
                        task.setTopic(taskEntity.getTopic());
                        task.setMessageId(taskEntity.getMessageId());
                        task.setMessage(JSON.toJSONString(taskEntity.getMessage()));
                        task.setState(taskEntity.getTaskStateVO().getCode());
                        taskDao.insert(task);
                    }
                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("写入返利记录，唯一索引冲突 userId: {}", userId, e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                }
            });
        } finally {
            dbRouter.clear();
        }

        // mq
        for (BehaviorRebateAggregate behaviorRebateAggregate : behaviorRebateAggregates) {
            BehaviorRebateTaskEntity behaviorRebateTaskEntity = behaviorRebateAggregate.getBehaviorRebateTaskEntity();
            Task task = Task.builder()
                        .userId(behaviorRebateTaskEntity.getUserId())
                        .messageId(behaviorRebateTaskEntity.getMessageId())
                        .build();
            try{
                eventPublisher.publish(behaviorRebateTaskEntity.getTopic(), behaviorRebateTaskEntity.getMessage());
                taskDao.updateTaskSendMessageCompleted(task);
            }catch (Exception e){
                taskDao.updateTaskSendMessageFail(task);
                log.error("mq 消息发送失败");
            }

        }


    }
}
