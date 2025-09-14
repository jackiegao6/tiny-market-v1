package com.gzc.domain.strategy.adapter.repository;


import com.gzc.domain.strategy.model.entity.StrategyAwardEntity;
import com.gzc.domain.strategy.model.entity.StrategyEntity;
import com.gzc.domain.strategy.model.entity.StrategyRuleEntity;
import com.gzc.domain.strategy.model.valobj.RuleTreeVO;
import com.gzc.domain.strategy.model.valobj.RuleWeightVO;
import com.gzc.domain.strategy.model.valobj.StrategyAwardTreeRootVO;
import com.gzc.domain.strategy.model.valobj.StrategyAwardStockKeyVO;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description 策略服务仓储接口
 */
public interface IStrategyRepository {

    /**
     * domain: armory
     */
    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    boolean hasSearchRateTable(String cacheStrategyAwards);

    <K, V> void storeSearchRateTable(String armoryAwardsKey, Integer rateRange, Map<K, V> strategyAwardSearchRateTable);

    void cacheLotteryAward(String cacheKey, Integer awardCount);

    StrategyEntity queryStrategyEntityByStrategyId(Long strategyId);

    StrategyRuleEntity queryStrategyRuleEntityByStrategyId(Long strategyId, String ruleModel);


    /**
     * domain: draw
     */
    String queryStrategyRuleValue(Long strategyId, String ruleModel);

    Integer queryUserJoinCount(String userId, Long strategyId);

    StrategyAwardEntity queryStrategyAwardEntity(Long strategyId, Integer awardId);

    StrategyAwardTreeRootVO queryStrategyAwardRuleModelVO(Long strategyId, Integer awardId);

    RuleTreeVO queryRuleTreeVOByTreeId(String treeId);

    Boolean subtractionAwardStock(String cacheKey, Integer awardId);

    void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO);

    Integer queryTodayUserRaffleCount(String userId, Long strategyId);







    Integer getRandomAward(Long strategyId, Integer rateKey);

    Integer getRandomAward(String key, Integer rateKey);

    int getRateRange(Long strategyId);

    int getRateRange(String key);

    String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel);


    Boolean subtractionAwardStock(String cacheKey, Integer awardId, Date endDateTime);



    StrategyAwardStockKeyVO takeQueueValue();

    /**
     * 跟新数据库表的 库存信息
     */
    void updateStrategyAwardStock(Long strategyId, Integer awardId);


    Long queryStrategyIdByActivityId(Long activityId);


    Map<String, Integer> queryTreeLockCount(String[] treeIds);


    List<RuleWeightVO> queryRuleWeightDetails(Long strategyId);

    <K, V> Map<K, V> getMap(String key);

    String cacheAlgorithmKey(String name);

}
