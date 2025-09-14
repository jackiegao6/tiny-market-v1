package com.gzc.domain.strategy.service.armory.algorithm;

import com.gzc.domain.strategy.model.entity.StrategyAwardEntity;

import java.math.BigDecimal;
import java.util.List;

public interface IAlgorithm {

    void armoryAlgorithm(String key, List<StrategyAwardEntity> strategyAwardEntities, BigDecimal rateRange);

    Integer raffleAlgorithm(String key);
}
