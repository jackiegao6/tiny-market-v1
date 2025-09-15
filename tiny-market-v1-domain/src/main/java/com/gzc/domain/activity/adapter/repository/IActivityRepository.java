package com.gzc.domain.activity.adapter.repository;

import com.gzc.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import com.gzc.domain.activity.model.aggregate.partake.CreatePartakeOrderAggregate;
import com.gzc.domain.activity.model.entity.*;
import com.gzc.domain.activity.model.entity.partake.ActivityAccountDayEntity;
import com.gzc.domain.activity.model.entity.partake.ActivityAccountMonthEntity;
import com.gzc.domain.activity.model.entity.partake.PartakeRaffleActivityEntity;
import com.gzc.domain.activity.model.entity.partake.UserRaffleOrderEntity;
import com.gzc.domain.activity.model.valobj.ActivitySkuStockKeyVO;

import java.util.Date;
import java.util.List;

/**
 * @description 活动仓储接口
 */
public interface IActivityRepository {

    /**
     * domain: armory
     */
    ActivityEntity queryActivityInfoByActivityId(Long activityId);

    List<ActivitySkuEntity> queryActivitySkuListByActivityId(Long activityId);

    ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);


    /**
     * domain: partake
     */
    UserRaffleOrderEntity queryNoUsedRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);

    ActivityAccountEntity queryActivityAccountByUserId(String userId, Long activityId);

    void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate createPartakeOrderAggregate);

    /**
     * domain: recharge sku
     */
    UnpaidActivityOrderEntity queryUnpaidActivityOrder(SkuRechargeEntity skuRechargeEntity);

    ActivitySkuEntity queryActivitySku(Long sku);

    boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime);

    void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO build);

    void doSaveSkuRechargeOrder(CreateQuotaOrderAggregate createQuotaOrderAggregate);


    /**
     * domain: add credit
     */
    void doSaveCreditPayOrder(CreateQuotaOrderAggregate createQuotaOrderAggregate);

    void updateOrder(DeliveryOrderEntity deliveryOrderEntity);


    /**
     * domain: query personal sku count
     */
    ActivityAccountEntity queryActivityAccountEntity(Long activityId, String userId);





    ActivitySkuStockKeyVO takeQueueValue();

    void clearQueueValue();

    void updateActivitySkuStock(Long sku);

    void clearActivitySkuStock(Long sku);


    ActivityAccountMonthEntity queryActivityAccountMonthByUserId(String userId, Long activityId, String month);

    ActivityAccountDayEntity queryActivityAccountDayByUserId(String userId, Long activityId, String day);


    Integer queryRaffleActivityAccountDayPartakeCount(Long activityId, String userId);


    Integer queryRaffleActivityAccountPartakeCount(Long activityId, String userId);

    List<SkuProductEntity> querySkuProductEntityByActivityId(Long activityId);

}
