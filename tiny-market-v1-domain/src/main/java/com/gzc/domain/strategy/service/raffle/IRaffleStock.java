package com.gzc.domain.strategy.service.raffle;

import com.gzc.domain.strategy.model.valobj.StrategyAwardStockKeyVO;

public interface IRaffleStock {

    /**
     * 获取奖品库存消耗队列
     *
     * @return 奖品库存key信息
     */
    StrategyAwardStockKeyVO takeQueueValue();


    /**
     * 更新奖品库存记录
     */
    void updateStrategyAwardStock(Long strategyId, Integer awardId);

}
