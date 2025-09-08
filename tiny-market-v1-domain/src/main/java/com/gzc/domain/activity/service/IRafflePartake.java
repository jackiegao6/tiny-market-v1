package com.gzc.domain.activity.service;

import com.gzc.domain.activity.model.entity.partake.PartakeRaffleActivityEntity;
import com.gzc.domain.activity.model.entity.partake.UserRaffleOrderEntity;

public interface IRafflePartake {

    // 创建抽奖单：用户参与抽奖活动 扣减账户库存 创建抽奖单
    UserRaffleOrderEntity createRaffleOrder(PartakeRaffleActivityEntity entity);
}
