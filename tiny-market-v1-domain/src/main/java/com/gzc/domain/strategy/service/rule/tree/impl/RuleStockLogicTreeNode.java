package com.gzc.domain.strategy.service.rule.tree.impl;

import com.gzc.domain.strategy.adapter.repository.IStrategyRepository;
import com.gzc.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.gzc.domain.strategy.model.valobj.StrategyAwardStockKeyVO;
import com.gzc.domain.strategy.service.armory.IStrategyDispatch;
import com.gzc.domain.strategy.service.rule.tree.ILogicTreeNode;
import com.gzc.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @description 库存扣减节点
 */
@Slf4j
@Component("rule_stock")
public class RuleStockLogicTreeNode implements ILogicTreeNode {

    @Resource
    private IStrategyDispatch strategyDispatch;
    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId, String ruleValue, Date endDateTime) {

        Boolean status = strategyDispatch.subtractionAwardStock(strategyId, awardId, endDateTime);
        if (status) {
            log.info("规则过滤-库存扣减-成功 userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);
            // 写入延迟队列 延迟消费更新数据库记录 + job 任务消费
            strategyRepository.awardStockConsumeSendQueue(StrategyAwardStockKeyVO.builder()
                    .strategyId(strategyId)
                    .awardId(awardId)
                    .build());

            return DefaultTreeFactory.TreeActionEntity.builder()
                    .ruleLogicCheckType(RuleLogicCheckTypeVO.ALLOW)
                    .strategyAwardVO(DefaultTreeFactory.StrategyAwardVO.builder()
                            .awardId(awardId)
                            .build())
                    .build();
        }

        // 没拿到锁有两种情况
        // 1. 如果库存不足，则该奖品不能让你拿到
        // 2. 运营添加库存后，库存重新扣减到到已经上锁的奖品序列 比如 一开始有10个库存 经过一段时间 扣减到 5 此时把 10、9、8、7、6 都加了锁
        //    这时候运营想加5个库存 就把库存更新为15 15、14、13、12、11 都是没锁的 返回正常抽的奖品 10、9、8、7、6 是锁了的 返回兜底积分

        // 弊端：
        // 加库存，用incr比较好用，因为decr/incr是一次请求，setnx是一次请求
        //用decr先从10减到5，decr + setnx一共10次，恢复库存后还要经过这10次
        //用incr，先从0加到5，一共10次，后面不用再经过这10次
        log.warn("规则过滤-库存扣减-告警，库存不足。userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);
        return DefaultTreeFactory.TreeActionEntity.builder()
                .ruleLogicCheckType(RuleLogicCheckTypeVO.TAKE_OVER)
                .build();
    }

}
