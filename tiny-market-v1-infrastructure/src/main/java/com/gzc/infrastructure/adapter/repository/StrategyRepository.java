package com.gzc.infrastructure.adapter.repository;

import com.gzc.domain.strategy.adapter.repository.IStrategyRepository;
import com.gzc.domain.strategy.model.entity.StrategyAwardEntity;
import com.gzc.domain.strategy.model.entity.StrategyEntity;
import com.gzc.domain.strategy.model.entity.StrategyRuleEntity;
import com.gzc.domain.strategy.model.valobj.*;
import com.gzc.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.gzc.infrastructure.dao.*;
import com.gzc.infrastructure.dao.po.*;
import com.gzc.infrastructure.redis.IRedisService;
import com.gzc.types.common.Constants;
import com.gzc.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.gzc.types.common.Constants.RedisKey.STRATEGY_RULE_WEIGHT_KEY;
import static com.gzc.types.enums.ResponseCode.UN_ASSEMBLED_STRATEGY_ARMORY;

/**
 * @description 策略服务仓储实现
 */
@Slf4j
@Repository
public class StrategyRepository implements IStrategyRepository {

    @Resource
    private IRaffleActivityDao raffleActivityDao;
    @Resource
    private IStrategyAwardDao strategyAwardDao;
    @Resource
    private IRedisService redisService;
    @Resource
    private IStrategyDao strategyDao;
    @Resource
    private IStrategyRuleDao strategyRuleDao;
    @Resource
    private IRuleTreeDao ruleTreeDao;
    @Resource
    private IRuleTreeNodeDao ruleTreeNodeDao;
    @Resource
    private IRuleTreeNodeLineDao ruleTreeNodeLineDao;
    @Resource
    private IRaffleActivityAccountDayDao raffleActivityAccountDayDao;
    @Resource
    private IRaffleActivityAccountDao raffleActivityAccountDao;

    /**
     * 根据策略id
     * 获取该策略下的奖品信息
     */
    @Override
    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {

        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_LIST_KEY + strategyId;
        List<StrategyAwardEntity> strategyAwardEntities = redisService.getValue(cacheKey);
        if (null != strategyAwardEntities && !strategyAwardEntities.isEmpty()) return strategyAwardEntities;

        List<StrategyAward> strategyAwards = strategyAwardDao.queryStrategyAwardListByStrategyId(strategyId);
        strategyAwardEntities = new ArrayList<>(strategyAwards.size());
        for (StrategyAward strategyAward : strategyAwards) {
            StrategyAwardEntity strategyAwardEntity = StrategyAwardEntity.builder()
                    .strategyId(strategyAward.getStrategyId())
                    .awardId(strategyAward.getAwardId())
                    .awardTitle(strategyAward.getAwardTitle())
                    .awardSubtitle(strategyAward.getAwardSubtitle())
                    .awardCount(strategyAward.getAwardCount())
                    .awardCountSurplus(strategyAward.getAwardCountSurplus())
                    .awardRate(strategyAward.getAwardRate())
                    .sort(strategyAward.getSort())
                    .ruleModels(strategyAward.getRuleModels())
                    .build();
            strategyAwardEntities.add(strategyAwardEntity);
        }
        redisService.setValue(cacheKey, strategyAwardEntities);
        return strategyAwardEntities;
    }

    // 根据 策略id 获得相应的策略规则
    @Override
    public StrategyEntity queryStrategyEntityByStrategyId(Long strategyId) {
        String cacheKey = Constants.RedisKey.STRATEGY_KEY + strategyId;
        StrategyEntity strategyEntity = redisService.getValue(cacheKey);
        if (null != strategyEntity) return strategyEntity;

        Strategy strategy = strategyDao.queryStrategyByStrategyId(strategyId);
        strategyEntity = StrategyEntity.builder()
                .strategyId(strategy.getStrategyId())
                .strategyDesc(strategy.getStrategyDesc())
                .ruleModels(strategy.getRuleModels())
                .build();
        redisService.setValue(cacheKey, strategyEntity);
        return strategyEntity;
    }

    @Override
    public StrategyRuleEntity queryStrategyRuleEntityByStrategyId(Long strategyId, String ruleModel) {
        StrategyRule strategyRuleReq = new StrategyRule();
        strategyRuleReq.setStrategyId(strategyId);
        strategyRuleReq.setRuleModel(ruleModel);

        StrategyRule strategyRuleRes = strategyRuleDao.queryStrategyRule(strategyRuleReq);
        return StrategyRuleEntity.builder()
                .strategyId(strategyRuleRes.getStrategyId())
                .awardId(strategyRuleRes.getAwardId())
                .ruleType(strategyRuleRes.getRuleType())
                .ruleModel(strategyRuleRes.getRuleModel())
                .ruleValue(strategyRuleRes.getRuleValue())
                .ruleDesc(strategyRuleRes.getRuleDesc())
                .build();
    }

    @Override
    public String queryStrategyRuleValue(Long strategyId, String ruleModel) {
        return queryStrategyRuleValue(strategyId, null, ruleModel);
    }

    @Override
    public String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel) {

        String cacheKey = Constants.RedisKey.STRATEGY_RULE_MODE_KEY + ruleModel;
        String ruleValue = redisService.getValue(cacheKey);
        if (ruleValue != null) return ruleValue;

        StrategyRule strategyRule = new StrategyRule();
        strategyRule.setStrategyId(strategyId);
        strategyRule.setAwardId(awardId);
        strategyRule.setRuleModel(ruleModel);

        ruleValue = strategyRuleDao.queryStrategyRuleValue(strategyRule);
        redisService.setValue(cacheKey, ruleValue);
        return ruleValue;
    }

    @Override
    public StrategyAwardTreeRootVO queryStrategyAwardRuleModelVO(Long strategyId, Integer awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);

        String treeId = strategyAwardDao.queryStrategyAwardRuleModels(strategyAward);
        return StrategyAwardTreeRootVO.builder()
                .treeId(treeId)
                .build();
    }

    @Override
    public StrategyAwardEntity queryStrategyAwardEntity(Long strategyId, Integer awardId) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_KEY + strategyId + Constants.COLON + awardId;
        StrategyAwardEntity value = redisService.getValue(cacheKey);
        if (value != null) return value;

        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        StrategyAward strategyAward1 = strategyAwardDao.queryStrategyAward(strategyAward);
        StrategyAwardEntity res = StrategyAwardEntity.builder()
                .strategyId(strategyAward1.getStrategyId())
                .awardId(strategyAward1.getAwardId())
                .awardTitle(strategyAward1.getAwardTitle())
                .awardSubtitle(strategyAward1.getAwardSubtitle())
                .awardCount(strategyAward1.getAwardCount())
                .awardCountSurplus(strategyAward1.getAwardCountSurplus())
                .awardRate(strategyAward1.getAwardRate())
                .sort(strategyAward1.getSort())
                .build();
        redisService.setValue(cacheKey, res, 86400000);
        return res;
    }

    @Override
    public boolean hasSearchRateTable(String cacheStrategyAwards) {
        Map<Integer, Integer> cacheRateTable = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + cacheStrategyAwards);
        return !cacheRateTable.isEmpty();
    }

    @Override
    public <K, V> void storeSearchRateTable(String armoryAwardsKey, Integer rateRange, Map<K, V> strategyAwardSearchRateTable) {
        // 1. 存储抽奖策略范围值，如10000，用于生成1000以内的随机数
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + armoryAwardsKey, rateRange);
        // 2. 存储概率查找表
        Map<K, V> cacheRateTable = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + armoryAwardsKey);
        if (cacheRateTable.isEmpty()) {
            cacheRateTable.putAll(strategyAwardSearchRateTable);
        }
    }

    @Override
    public Integer getRandomAward(Long strategyId, Integer awardIndex) {
        return getRandomAward(String.valueOf(strategyId), awardIndex);
    }

    @Override
    public Integer getRandomAward(String key, Integer awardIndex) {
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key, awardIndex);
    }

    @Override
    public int getRateRange(Long strategyId) {
        return getRateRange(String.valueOf(strategyId));
    }

    @Override
    public int getRateRange(String key) {
        String cacheKey = Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key;
        if (!redisService.isExists(cacheKey)) {
            throw new AppException(UN_ASSEMBLED_STRATEGY_ARMORY.getCode(), cacheKey + Constants.COLON + UN_ASSEMBLED_STRATEGY_ARMORY.getInfo());
        }
        return redisService.getValue(cacheKey);
    }

    @Override
    public RuleTreeVO queryRuleTreeVOByTreeId(String treeId) {
        // 优先从缓存获取
        String cacheKey = Constants.RedisKey.RULE_TREE_VO_KEY + treeId;
        RuleTreeVO ruleTreeVOCache = redisService.getValue(cacheKey);
        if (null != ruleTreeVOCache) return ruleTreeVOCache;

        // 从数据库获取
        RuleTree ruleTree = ruleTreeDao.queryRuleTreeByTreeId(treeId);
        List<RuleTreeNode> ruleTreeNodes = ruleTreeNodeDao.queryRuleTreeNodeListByTreeId(treeId);
        List<RuleTreeNodeLine> ruleTreeNodeLines = ruleTreeNodeLineDao.queryRuleTreeNodeLineListByTreeId(treeId);

        // 1. tree node line 转换Map结构
        Map<String, List<RuleTreeNodeLineVO>> ruleTreeNodeLineMap = new HashMap<>();
        for (RuleTreeNodeLine ruleTreeNodeLine : ruleTreeNodeLines) {
            RuleTreeNodeLineVO ruleTreeNodeLineVO = RuleTreeNodeLineVO.builder()
                    .treeId(ruleTreeNodeLine.getTreeId())
                    .ruleNodeFrom(ruleTreeNodeLine.getRuleNodeFrom())
                    .ruleNodeTo(ruleTreeNodeLine.getRuleNodeTo())
                    .ruleLimitType(RuleLimitTypeVO.valueOf(ruleTreeNodeLine.getRuleLimitType()))
                    .ruleLimitValue(RuleLogicCheckTypeVO.valueOf(ruleTreeNodeLine.getRuleLimitValue()))
                    .build();

            List<RuleTreeNodeLineVO> ruleTreeNodeLineVOList = ruleTreeNodeLineMap.computeIfAbsent(ruleTreeNodeLine.getRuleNodeFrom(), k -> new ArrayList<>());
            ruleTreeNodeLineVOList.add(ruleTreeNodeLineVO);
        }

        // 2. tree node 转换为Map结构
        Map<String, RuleTreeNodeVO> treeNodeMap = new HashMap<>();
        for (RuleTreeNode ruleTreeNode : ruleTreeNodes) {
            RuleTreeNodeVO ruleTreeNodeVO = RuleTreeNodeVO.builder()
                    .treeId(ruleTreeNode.getTreeId())
                    .ruleKey(ruleTreeNode.getRuleKey())
                    .ruleDesc(ruleTreeNode.getRuleDesc())
                    .ruleValue(ruleTreeNode.getRuleValue())
                    .treeNodeLineVOList(ruleTreeNodeLineMap.get(ruleTreeNode.getRuleKey()))
                    .build();
            treeNodeMap.put(ruleTreeNode.getRuleKey(), ruleTreeNodeVO);
        }

        // 3. 构建 Rule Tree
        RuleTreeVO ruleTreeVODB = RuleTreeVO.builder()
                .treeId(ruleTree.getTreeId())
                .treeName(ruleTree.getTreeName())
                .treeDesc(ruleTree.getTreeDesc())
                .treeRootRuleNode(ruleTree.getTreeRootRuleKey())
                .treeNodeMap(treeNodeMap)
                .build();

        redisService.setValue(cacheKey, ruleTreeVODB);
        return ruleTreeVODB;
    }

    @Override
    public void cacheLotteryAward(String cacheKey, Integer awardCount) {
        if (redisService.isExists(cacheKey)) return;

        redisService.setAtomicLong(cacheKey, awardCount);
    }

    @Override
    public Boolean subtractionAwardStock(String cacheKey, Integer awardId) {
        return subtractionAwardStock(cacheKey, awardId, null);
    }

    @Override
    public Boolean subtractionAwardStock(String cacheKey, Integer awardId, Date endDateTime) {
        long surplus = redisService.decr(cacheKey);
        if (surplus < 0) {
            redisService.setValue(cacheKey, 0);
            return false;
        }
        String lockKey = cacheKey + Constants.UNDERLINE + surplus;
        Boolean lock = false;
        if (null != endDateTime) {
            long expireMillis = endDateTime.getTime() - System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
            lock = redisService.setNx(lockKey, expireMillis, TimeUnit.MILLISECONDS);
        } else {
            lock = redisService.setNx(lockKey);
        }
        if (!lock) {
            log.info("策略奖品 库存获取锁失败 {}", lockKey);
        }
        return lock;
    }

    // todo 更改为mq的延迟队列
    @Override
    public void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_CONSUME_Q;
        RBlockingQueue<StrategyAwardStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);

        RDelayedQueue<StrategyAwardStockKeyVO> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        delayedQueue.offer(strategyAwardStockKeyVO, 3, TimeUnit.SECONDS);

    }

    @Override
    public StrategyAwardStockKeyVO takeQueueValue() {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_CONSUME_Q;
        RBlockingQueue<StrategyAwardStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);

        return blockingQueue.poll();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);

        strategyAwardDao.updateStrategyAwardStock(strategyAward);
    }

    @Override
    public Long queryStrategyIdByActivityId(Long activityId) {
        String cacheKey = Constants.RedisKey.STRATEGY_ID4ACTIVITY_KEY + activityId;
        Long strategyId = redisService.getValue(cacheKey);
        if (strategyId == null){
            strategyId = raffleActivityDao.queryActivityIdByStrategyId(activityId);
            redisService.setValue(cacheKey, strategyId);
        }
        return strategyId;
    }

    @Override
    public Integer queryTodayUserRaffleCount(String userId, Long strategyId) {

        String cacheKey = Constants.RedisKey.ACTIVITY_ID4STRATEGY_KEY + strategyId;
        Long activityId = redisService.getValue(cacheKey);
        if (activityId == null){
            activityId = raffleActivityDao.queryActivityIdByStrategyId(strategyId);
            redisService.setValue(cacheKey, activityId);
        }
        // 封装参数
        RaffleActivityAccountDay raffleActivityAccountDayReq = new RaffleActivityAccountDay();
        raffleActivityAccountDayReq.setUserId(userId);
        raffleActivityAccountDayReq.setActivityId(activityId);
        raffleActivityAccountDayReq.setDay(raffleActivityAccountDayReq.currentDay());
        RaffleActivityAccountDay raffleActivityAccountDay = raffleActivityAccountDayDao.queryActivityAccountDayByUserId(raffleActivityAccountDayReq);
        if (null == raffleActivityAccountDay) return 0;
        // 总次数 - 剩余的，等于今日参与的
        return raffleActivityAccountDay.getDayCount() - raffleActivityAccountDay.getDayCountSurplus();
    }

    @Override
    public Map<String, Integer> queryTreeLockCount(String[] treeIds) {

        if (treeIds == null || treeIds.length == 0) return new HashMap<>();
        List<RuleTreeNode> ruleTreeNodes = ruleTreeNodeDao.queryRuleLocks(treeIds);
        HashMap<String, Integer> res = new HashMap<>();
        for (RuleTreeNode node : ruleTreeNodes) {
            String treeId = node.getTreeId();
            Integer ruleValue = Integer.valueOf(node.getRuleValue());
            res.put(treeId, ruleValue);
        }

        return res;
    }

    @Override
    public Integer queryUserJoinCount(String userId, Long strategyId) {
        String cacheKey = Constants.RedisKey.ACTIVITY_ID4STRATEGY_KEY + strategyId;
        Long activityId = redisService.getValue(cacheKey);
        if (activityId == null){
            activityId = raffleActivityDao.queryActivityIdByStrategyId(strategyId);
            redisService.setValue(cacheKey, activityId);
        }

        RaffleActivityAccount raffleActivityAccount = raffleActivityAccountDao.queryActivityAccountByUserId(RaffleActivityAccount.builder()
                .userId(userId)
                .activityId(activityId)
                .build());

        return raffleActivityAccount.getTotalCount() - raffleActivityAccount.getTotalCountSurplus();
    }

    @Override
    public List<RuleWeightVO> queryRuleWeightDetails(Long strategyId) {
        String cacheKey = STRATEGY_RULE_WEIGHT_KEY + strategyId;
        List<RuleWeightVO> ruleWeightVOS = redisService.getValue(cacheKey);
        if (null != ruleWeightVOS) return ruleWeightVOS;

        // 1. 查询权重规则配置
        ruleWeightVOS = new ArrayList<>();
        StrategyRule strategyRuleReq = new StrategyRule();
        strategyRuleReq.setStrategyId(strategyId);
        strategyRuleReq.setRuleModel(DefaultChainFactory.LogicModel.RULE_WEIGHT.getCode());
        String ruleValue = strategyRuleDao.queryStrategyRuleValue(strategyRuleReq);

        // 2. 借助实体对象转换规则
        StrategyRuleEntity strategyRuleEntity = new StrategyRuleEntity();
        strategyRuleEntity.setRuleModel(DefaultChainFactory.LogicModel.RULE_WEIGHT.getCode());
        strategyRuleEntity.setRuleValue(ruleValue);

        Map<String, List<Integer>> weightThreshold2Awards = strategyRuleEntity.getRuleWeightValues();
        Set<String> thresholds = weightThreshold2Awards.keySet();
        for (String threshold : thresholds) {
            List<Integer> awardIds = weightThreshold2Awards.get(threshold);
            ArrayList<RuleWeightVO.Award> awards = new ArrayList<>();
            List<StrategyAward> strategyAwards = strategyAwardDao.queryStrategyAwardListByAwardIds(awardIds);
            for (StrategyAward strategyAward : strategyAwards) {
                awards.add(RuleWeightVO.Award.builder()
                        .awardId(strategyAward.getAwardId())
                        .awardTitle(strategyAward.getAwardTitle())
                        .build());
            }
            ruleWeightVOS.add(RuleWeightVO.builder()
                    .weight(Integer.valueOf(threshold))
                    .ruleValue(ruleValue)
                    .awardIds(awardIds)
                    .awardList(awards)
                    .build());
        }
        // 设置缓存 在活动下架时同一清空缓存
        redisService.setValue(cacheKey, ruleWeightVOS);
        return ruleWeightVOS;
    }

    @Override
    public <K, V> Map<K, V> getMap(String key) {
        return redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key);
    }

    @Override
    public String cacheAlgorithmKey(String name) {
        Object value = redisService.getValue(Constants.RedisKey.STRATEGY_ALGORITHM_KEY);
        if (value != null) return (String) value;
        redisService.setValue(Constants.RedisKey.STRATEGY_ALGORITHM_KEY, name);
        return null;
    }
}
