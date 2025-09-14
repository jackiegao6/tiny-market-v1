package com.gzc.domain.activity.service.armory;

import com.gzc.domain.activity.adapter.repository.IActivityRepository;
import com.gzc.domain.activity.model.entity.ActivityCountEntity;
import com.gzc.domain.activity.model.entity.ActivityEntity;
import com.gzc.domain.activity.model.entity.ActivitySkuEntity;
import com.gzc.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ActivityArmory implements IActivityArmory, IActivityDispatch {

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public boolean assembleActivitySkuByActivityId(Long activityId) {
        // 0. 缓存该活动的信息
        ActivityEntity activityEntity = activityRepository.queryActivityInfoByActivityId(activityId);
        // 1. 缓存该活动配置的所有sku组
        List<ActivitySkuEntity> activitySkuEntities = activityRepository.queryActivitySkuListByActivityId(activityId);
        for (ActivitySkuEntity activitySkuEntity : activitySkuEntities) {
            // 2. 缓存该sku组 对应的个人抽取次数信息
            ActivityCountEntity activityCountEntity = activityRepository.queryRaffleActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());
        }
        return true;
    }

    @Override
    public boolean subtractionActivitySkuStock(Long sku, Date endDateTime) {
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_STOCK_SURPLUS_KEY + sku;
        return activityRepository.subtractionActivitySkuStock(sku, cacheKey, endDateTime);
    }
}
