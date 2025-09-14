package com.gzc.domain.strategy.service.raffle;

import com.gzc.domain.strategy.adapter.repository.IStrategyRepository;
import com.gzc.domain.strategy.model.entity.RaffleAwardEntity;
import com.gzc.domain.strategy.model.entity.RaffleFactorEntity;
import com.gzc.domain.strategy.model.entity.StrategyAwardEntity;
import com.gzc.domain.strategy.service.armory.IStrategyDispatch;
import com.gzc.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.gzc.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import com.gzc.types.enums.ResponseCode;
import com.gzc.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖策略抽象类，定义抽奖的标准流程
 * @create 2024-01-06 09:26
 */
@Slf4j
public abstract class AbstractRaffleStrategy implements IRaffleStrategy {

    protected final IStrategyRepository strategyRepository;
    protected final IStrategyDispatch strategyDispatch;
    protected final DefaultChainFactory defaultChainFactory;
    protected final DefaultTreeFactory defaultTreeFactory;

    public AbstractRaffleStrategy(IStrategyRepository repository, IStrategyDispatch strategyDispatch, DefaultChainFactory defaultChainFactory, DefaultTreeFactory defaultTreeFactory) {
        this.strategyRepository = repository;
        this.strategyDispatch = strategyDispatch;
        this.defaultChainFactory = defaultChainFactory;
        this.defaultTreeFactory = defaultTreeFactory;
    }

    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity) {

        String userId = raffleFactorEntity.getUserId();
        Long strategyId = raffleFactorEntity.getStrategyId();
        if (null == strategyId || StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        /**
         * 串联调用流程。
         * 先是责任链，后是规则树。
         * 责任链处理的是不同的抽奖【黑名单、权重、默认】，处理完的抽奖结果
         * 如果是默认抽奖则需要进行库存、次数等校验，并给出最终发奖结果。
         */
        DefaultChainFactory.StrategyAwardVO chainStrategyAwardVO = raffleLogicChain(userId, strategyId);
        if (!DefaultChainFactory.LogicModel.RULE_DEFAULT.getCode().equals(chainStrategyAwardVO.getLogicModel())) {
            // 被权重抽奖或黑名单抽奖规则接管
            return buildRaffleAwardEntity(strategyId, chainStrategyAwardVO.getAwardId(), chainStrategyAwardVO.getAwardRuleValue());
        }

        DefaultTreeFactory.StrategyAwardVO treeStrategyAwardVO = raffleLogicTree(userId, chainStrategyAwardVO.getAwardId(), strategyId, raffleFactorEntity.getEndDateTime());

        return buildRaffleAwardEntity(strategyId, treeStrategyAwardVO.getAwardId(), treeStrategyAwardVO.getAwardRuleValue());
    }

    private RaffleAwardEntity buildRaffleAwardEntity(Long strategyId, Integer awardId, String awardConfig) {
        StrategyAwardEntity strategyAward = strategyRepository.queryStrategyAwardEntity(strategyId, awardId);
        return RaffleAwardEntity.builder()
                .awardId(awardId)
                .awardConfig(awardConfig)
                .awardTitle(strategyAward.getAwardTitle())
                .sort(strategyAward.getSort())
                .build();
    }

    public abstract DefaultChainFactory.StrategyAwardVO raffleLogicChain(String userId, Long strategyId);

    public abstract DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Integer awardId, Long strategyId);

    public abstract DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Integer awardId, Long strategyId, Date endDateTime);

}
