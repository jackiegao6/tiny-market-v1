package com.gzc.domain.activity.service;


import com.gzc.domain.activity.model.valobj.ActivitySkuStockKeyVO;

/**
 * @description 活动sku库存处理接口
 */
public interface ISkuStock {

    /**
     * 获取活动sku库存消耗队列
     *
     * @return 奖品库存Key信息
     * @throws InterruptedException 异常
     */
    ActivitySkuStockKeyVO skuStockConsumeSendQueueValue() throws InterruptedException;

    /**
     * 延迟队列 + 任务趋势更新活动sku库存
     *
     * @param sku 活动商品
     */
    void updateActivitySkuStock(Long sku);

    /**
     * 清空队列
     */
    void clearSkuStockQueueValue();

    /**
     * 缓存库存以消耗完毕，清空数据库库存
     *
     * @param sku 活动商品
     */
    void clearActivitySkuStock(Long sku);

}
