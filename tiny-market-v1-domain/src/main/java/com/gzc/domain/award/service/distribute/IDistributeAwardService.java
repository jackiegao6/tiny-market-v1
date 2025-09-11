package com.gzc.domain.award.service.distribute;


import com.gzc.domain.award.model.entity.DistributeAwardEntity;

/**
 * @description 分发奖品接口
 */
public interface IDistributeAwardService {

    void giveOutPrizes(DistributeAwardEntity distributeAwardEntity);

}