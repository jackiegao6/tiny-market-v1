package com.gzc.domain.strategy.service.armory.algorithm;

import com.gzc.domain.strategy.adapter.repository.IStrategyRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Resource;
import java.security.SecureRandom;

public abstract class AbstractAlgorithm implements IAlgorithm{

    @Resource
    protected IStrategyRepository strategyRepository;

    protected final SecureRandom secureRandom = new SecureRandom();

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public enum Algorithm{
        O1("O1"),
        OLogN("OLogN"),
        ;
        private String name;
    }

}
