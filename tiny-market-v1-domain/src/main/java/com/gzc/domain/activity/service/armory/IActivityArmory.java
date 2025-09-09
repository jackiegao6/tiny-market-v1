package com.gzc.domain.activity.service.armory;

/**
 * @description 活动装配预热
 * 活动涉及到的sku信息
 * 活动本身的信息
 * count?
 */
public interface IActivityArmory {
    boolean assembleActivitySku(Long sku);

    boolean assembleActivitySkuByActivityId(Long activityId);
}
