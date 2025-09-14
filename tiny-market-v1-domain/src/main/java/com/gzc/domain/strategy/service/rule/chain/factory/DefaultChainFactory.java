package com.gzc.domain.strategy.service.rule.chain.factory;

import com.gzc.domain.strategy.adapter.repository.IStrategyRepository;
import com.gzc.domain.strategy.model.entity.StrategyEntity;
import com.gzc.domain.strategy.service.rule.chain.ILogicChain;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @description 工厂
 */
@Service
public class DefaultChainFactory {

    private final Map<String, ILogicChain> logicChainGroup;
    protected IStrategyRepository strategyRepository;

    public DefaultChainFactory(Map<String, ILogicChain> logicChainGroup, IStrategyRepository repository) {
        this.logicChainGroup = logicChainGroup;
        this.strategyRepository = repository;
    }

    /**
     * 通过策略ID，构建责任链
     *
     * @param strategyId 策略ID
     * @return LogicChain
     */
    public ILogicChain buildLogicChain(Long strategyId) {
        StrategyEntity strategy = strategyRepository.queryStrategyEntityByStrategyId(strategyId);
        String[] ruleModels = strategy.getRuleModels();

        // 如果未配置策略规则，则只装填一个默认责任链
        if (null == ruleModels || 0 == ruleModels.length)
            return logicChainGroup.get("default");

        // 按照配置顺序装填用户配置的责任链；rule_blacklist、rule_weight
        ILogicChain headerChain = logicChainGroup.get(ruleModels[0]);
        ILogicChain pointer = headerChain;
        for (int i = 1; i < ruleModels.length; i++) {
            ILogicChain nextChain = logicChainGroup.get(ruleModels[i]);
            pointer = pointer.appendNext(nextChain);
        }

        // 责任链的最后装填默认责任链
        pointer.appendNext(logicChainGroup.get("default"));

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
