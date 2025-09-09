package com.gzc.domain.strategy.service.raffle;

import java.util.Map;

public interface IRaffleRule {

    Map<String, Integer> queryAwardRuleLockCount(String[] treeIds);
}
