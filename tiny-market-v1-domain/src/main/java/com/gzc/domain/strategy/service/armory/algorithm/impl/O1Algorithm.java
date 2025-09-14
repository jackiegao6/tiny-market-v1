package com.gzc.domain.strategy.service.armory.algorithm.impl;

import com.gzc.domain.strategy.model.entity.StrategyAwardEntity;
import com.gzc.domain.strategy.service.armory.algorithm.AbstractAlgorithm;
import com.gzc.domain.strategy.service.armory.algorithm.IAlgorithm;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service("O1")
public class O1Algorithm extends AbstractAlgorithm implements IAlgorithm {

    @Override
    public void armoryAlgorithm(String strategyKey, List<StrategyAwardEntity> strategyAwardEntities, BigDecimal rateRange) {

        List<Integer> strategyAwardSearchRateList = new ArrayList<>(rateRange.intValue());
        for (StrategyAwardEntity strategyAward : strategyAwardEntities) {
            Integer awardId = strategyAward.getAwardId();
            BigDecimal awardRate = strategyAward.getAwardRate();
            // 计算出每个概率值需要存放到查找表的数量，循环填充
            for (int i = 0; i < rateRange.multiply(awardRate).setScale(0, RoundingMode.CEILING).intValue(); i++) {
                strategyAwardSearchRateList.add(awardId);
            }
        }

        Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = new HashMap<>();
        for (int i = 0; i < strategyAwardSearchRateList.size(); i++) {
            shuffleStrategyAwardSearchRateTable.put(i, strategyAwardSearchRateList.get(i));
        }

        strategyRepository.storeSearchRateTable(strategyKey, shuffleStrategyAwardSearchRateTable.size(), shuffleStrategyAwardSearchRateTable);
    }

    @Override
    public Integer raffleAlgorithm(String key) {
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取。
        int rateRange = strategyRepository.getRateRange(key);
        // 通过生成的随机值，获取概率值奖品查找表的结果
        return strategyRepository.getRandomAward(key, secureRandom.nextInt(rateRange));
    }
}
