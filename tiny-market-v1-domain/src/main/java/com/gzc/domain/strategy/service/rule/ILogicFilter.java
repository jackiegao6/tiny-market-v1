package com.gzc.domain.strategy.service.rule;

import com.gzc.domain.strategy.model.entity.RuleActionEntity;
import com.gzc.domain.strategy.model.entity.RuleMatterEntity;

public interface ILogicFilter<T extends RuleActionEntity.RaffleEntity> {

    RuleActionEntity<T> filter(RuleMatterEntity ruleMatterEntity);
}
