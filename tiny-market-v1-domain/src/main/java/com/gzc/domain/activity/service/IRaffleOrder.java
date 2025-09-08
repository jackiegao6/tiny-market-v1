package com.gzc.domain.activity.service;

import com.gzc.domain.activity.model.entity.ActivityOrderEntity;
import com.gzc.domain.activity.model.entity.ActivityShopCartEntity;

public interface IRaffleOrder {

    ActivityOrderEntity createRaffleActivityOrder(ActivityShopCartEntity activityShopCartEntity);
}
