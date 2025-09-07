package com.gzc.domain.strategy.adapter.repository;


import com.gzc.domain.strategy.model.entity.StrategyAwardEntity;
import com.gzc.domain.strategy.model.entity.StrategyEntity;
import com.gzc.domain.strategy.model.entity.StrategyRuleEntity;
import com.gzc.domain.strategy.model.valobj.RuleTreeVO;
import com.gzc.domain.strategy.model.valobj.StrategyAwardRuleModelVO;
import com.gzc.domain.strategy.model.valobj.StrategyAwardStockKeyVO;

import java.util.List;
import java.util.Map;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 策略服务仓储接口
 * @create 2023-12-23 09:33
 */
public interface IStrategyRepository {

    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    void storeStrategyAwardSearchRateTable(String armoryAwardsKey, Integer rateRange, Map<Integer, Integer> strategyAwardSearchRateTable);

    Integer getRandomAward(Long strategyId, Integer rateKey);

    Integer getRandomAward(String key, Integer rateKey);

    int getRateRange(Long strategyId);

    int getRateRange(String key);

    StrategyEntity queryStrategyEntityByStrategyId(Long strategyId);

    StrategyRuleEntity queryStrategyRuleEntityByStrategyId(Long strategyId, String ruleModel);

    boolean hasStrategyAwardSearchRateTable(String armoryAwardsKey);

    String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel);

    String queryStrategyRuleValue(Long strategyId, String ruleModel);

    StrategyAwardRuleModelVO queryStrategyAwardRuleModelVO(Long strategyId, Integer awardId);

    /**
     * 根据规则树ID，查询树结构信息
     *
     * @param treeId 规则树ID
     * @return 树结构信息
     */
    RuleTreeVO queryRuleTreeVOByTreeId(String treeId);

    /**
     * 缓存奖品库存
     */
    void cacheLotteryAward(String cacheKey, Integer awardCount);

    /**
     * 扣减奖品库存
     */
    Boolean subtractionAwardStock(String cacheKey, Integer awardId);

    void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO);

    StrategyAwardStockKeyVO takeQueueValue();

    /**
     * 跟新数据库表的 库存信息
     */
    void updateStrategyAwardStock(Long strategyId, Integer awardId);

    StrategyAwardEntity queryStrategyAwardEntity(Long strategyId, Integer awardId);

}
