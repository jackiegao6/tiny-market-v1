package com.gzc.domain.strategy.service.rule.chain.factory;

import com.gzc.domain.strategy.adapter.repository.IStrategyRepository;
import com.gzc.domain.strategy.model.entity.StrategyEntity;
import com.gzc.domain.strategy.service.rule.chain.ILogicChain;
import lombok.*;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description 工厂
 */
@Service
public class DefaultChainFactory {

    private final ApplicationContext applicationContext;
    private final Map<Long, ILogicChain> strategyChainGroup;
    protected IStrategyRepository strategyRepository;

    public DefaultChainFactory(ApplicationContext applicationContext, IStrategyRepository strategyRepository) {
        this.applicationContext = applicationContext;
        this.strategyRepository = strategyRepository;
        this.strategyChainGroup = new ConcurrentHashMap<>();
    }

    /**
     * 通过策略ID，构建责任链
     *
     * @param strategyId 策略ID
     * @return LogicChain
     */
    public ILogicChain buildLogicChain(Long strategyId) {

        ILogicChain cacheLogicChain = strategyChainGroup.get(strategyId);
        if (cacheLogicChain != null) return cacheLogicChain;

        StrategyEntity strategy = strategyRepository.queryStrategyEntityByStrategyId(strategyId);
        String[] ruleModels = strategy.getRuleModels();

        // 如果未配置策略规则，则只装填一个默认责任链
        if (null == ruleModels || 0 == ruleModels.length){
            ILogicChain defaultLogicChain = applicationContext.getBean(LogicModel.RULE_DEFAULT.getCode(), ILogicChain.class);
            strategyChainGroup.put(strategyId, defaultLogicChain);
            return defaultLogicChain;
        }

        // 按照配置顺序装填用户配置的责任链；rule_blacklist、rule_weight
        ILogicChain headerChain = applicationContext.getBean(ruleModels[0], ILogicChain.class);
        ILogicChain pointer = headerChain;
        for (int i = 1; i < ruleModels.length; i++) {
            ILogicChain nextChain = applicationContext.getBean(ruleModels[i], ILogicChain.class);
            pointer = pointer.appendNext(nextChain);
        }

        // 责任链的最后装填默认责任链
        pointer.appendNext(applicationContext.getBean(LogicModel.RULE_DEFAULT.getCode(), ILogicChain.class));
        strategyChainGroup.put(strategyId, headerChain);
        return headerChain;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StrategyAwardVO {
        /** 抽奖奖品ID - 内部流转使用 */
        private Integer awardId;
        /**  */
        private String logicModel;
        private String awardRuleValue;
    }

    @Getter
    @AllArgsConstructor
    public enum LogicModel {

        RULE_DEFAULT("rule_default", "默认抽奖"),
        RULE_BLACKLIST("rule_blacklist", "黑名单抽奖"),
        RULE_WEIGHT("rule_weight", "权重规则"),
        ;

        private final String code;
        private final String info;

    }

}
