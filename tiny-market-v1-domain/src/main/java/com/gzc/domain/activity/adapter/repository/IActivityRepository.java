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




    ActivitySkuEntity queryActivitySku(Long sku);

    void doSaveSkuRechargeOrder(CreateQuotaOrderAggregate createQuotaOrderAggregate);

    void doSaveCreditPayOrder(CreateQuotaOrderAggregate createQuotaOrderAggregate);

    boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime);

    void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO build);

    ActivitySkuStockKeyVO takeQueueValue();

    void clearQueueValue();

    void updateActivitySkuStock(Long sku);

    void clearActivitySkuStock(Long sku);

    UserRaffleOrderEntity queryNoUsedRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);

    ActivityAccountEntity queryActivityAccountByUserId(String userId, Long activityId);

    ActivityAccountMonthEntity queryActivityAccountMonthByUserId(String userId, Long activityId, String month);

    ActivityAccountDayEntity queryActivityAccountDayByUserId(String userId, Long activityId, String day);

    void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate createPartakeOrderAggregate);

    Integer queryRaffleActivityAccountDayPartakeCount(Long activityId, String userId);

    ActivityAccountEntity queryActivityAccountEntity(Long activityId, String userId);

    Integer queryRaffleActivityAccountPartakeCount(Long activityId, String userId);

    void updateOrder(DeliveryOrderEntity deliveryOrderEntity);

    UnpaidActivityOrderEntity queryUnpaidActivityOrder(SkuRechargeEntity skuRechargeEntity);

    List<SkuProductEntity> querySkuProductEntityByActivityId(Long activityId);

}
