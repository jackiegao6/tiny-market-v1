package com.gzc.domain.strategy.service.rule.chain.impl;

import com.gzc.domain.strategy.adapter.repository.IStrategyRepository;
import com.gzc.domain.strategy.service.armory.IStrategyDispatch;
import com.gzc.domain.strategy.service.rule.chain.AbstractLogicChain;
import com.gzc.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.gzc.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 权重抽奖责任链
 * @create 2024-01-20 10:38
 */
@Slf4j
@Component("rule_weight")
public class RuleWeightLogicChain extends AbstractLogicChain {

    @Resource
    private IStrategyRepository strategyRepository;

    @Resource
    protected IStrategyDispatch strategyDispatch;

    /**
     * 权重责任链过滤；
     * 1. 权重规则格式；4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109
     * 2. 解析数据格式；判断哪个范围符合用户的特定抽奖范围
     */
    @Override
    public DefaultChainFactory.StrategyAwardVO logic(String userId, Long strategyId) {
        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, ruleModel());
        if (StringUtils.isBlank(ruleValue)){
            // 该策略并未配置权重规则
            return next().logic(userId, strategyId);
        }
        // 1. 根据用户ID查询用户抽奖消耗的积分值
        Map<Long, String> analyticalValueGroup = getAnalyticalValue(ruleValue);
        if (null == analyticalValueGroup || analyticalValueGroup.isEmpty()) return null;

        // 2. 转换Keys值，并默认排序
        List<Long> analyticalSortedKeys = new ArrayList<>(analyticalValueGroup.keySet());
        Collections.sort(analyticalSortedKeys);

        Integer userJoinCount = strategyRepository.queryUserJoinCount(userId, strategyId);

        // 3. 找出最小符合的值，也就是【4500 积分，能找到 4000:102,103,104,105】、【5000 积分，能找到 5000:102,103,104,105,106,107】
        Long suitableValue = analyticalSortedKeys.stream()
                .filter(key -> userJoinCount >= key)
                .findFirst()
                .orElse(null);

        // 4. 权重抽奖
        if (null != suitableValue) {
            Integer awardId = strategyDispatch.getRandomAwardId(strategyId, String.valueOf(suitableValue));
            log.info("抽奖责任链-权重接管 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, ruleModel(), awardId);
            return DefaultChainFactory.StrategyAwardVO.builder()
                    .awardId(awardId)
                    .logicModel(ruleModel())
                    .build();
        }

        return next().logic(userId, strategyId);
    }

    @Override
    protected String ruleModel() {
        return DefaultChainFactory.LogicModel.RULE_WEIGHT.getCode();
    }

    private Map<Long, String> getAnalyticalValue(String ruleValue) {
        String[] ruleValueGroups = ruleValue.split(Constants.SPACE);
        Map<Long, String> ruleValueMap = new HashMap<>();
        for (String ruleValueKey : ruleValueGroups) {
            // 检查输入是否为空
            if (ruleValueKey == null || ruleValueKey.isEmpty()) {
                return ruleValueMap;
            }
            // 分割字符串以获取键和值
            String[] parts = ruleValueKey.split(Constants.COLON);
            if (parts.length != 2) {
                throw new IllegalArgumentException("rule_weight rule_rule invalid input format" + ruleValueKey);
            }
            ruleValueMap.put(Long.parseLong(parts[0]), ruleValueKey);
        }
        return ruleValueMap;
    }

}
