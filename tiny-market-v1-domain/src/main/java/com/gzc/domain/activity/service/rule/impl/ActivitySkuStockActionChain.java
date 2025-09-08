package com.gzc.domain.activity.service.rule.impl;

import com.gzc.domain.activity.model.entity.ActivityCountEntity;
import com.gzc.domain.activity.model.entity.ActivityEntity;
import com.gzc.domain.activity.model.entity.ActivitySkuEntity;
import com.gzc.domain.activity.service.rule.AbstractActionChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 商品库存
 */
@Slf4j
@Service("activity_sku_stock_action")
public class ActivitySkuStockActionChain extends AbstractActionChain {

    @Override
    public Boolean logic(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {


        return Boolean.TRUE;
    }
}
