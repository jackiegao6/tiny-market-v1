package com.gzc.domain.activity.service.armory;

/**
 * @description 活动装配预热
 * 活动涉及到的sku信息
 * 活动本身的信息
 * 个人参与次数限制信息
 */
public interface IActivityArmory {

    @Deprecated
    boolean assembleActivitySku(Long sku);

    void assembleActivitySkuByActivityId(Long activityId);
}
