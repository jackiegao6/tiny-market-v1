package com.gzc.domain.activity.service.rule.impl;

import com.gzc.domain.activity.adapter.repository.IActivityRepository;
import com.gzc.domain.activity.model.entity.ActivityCountEntity;
import com.gzc.domain.activity.model.entity.ActivityEntity;
import com.gzc.domain.activity.model.entity.ActivitySkuEntity;
import com.gzc.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import com.gzc.domain.activity.service.armory.IActivityDispatch;
import com.gzc.domain.activity.service.rule.AbstractActionChain;
import com.gzc.types.enums.ResponseCode;
import com.gzc.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 商品库存
 */
@Slf4j
@Service("activity_sku_stock_action")
public class ActivitySkuStockActionChain extends AbstractActionChain {

    @Resource
    private IActivityDispatch activityDispatch;
    @Resource
    private IActivityRepository activityRepository;

    @Override
    public Boolean logic(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
        Long sku = activitySkuEntity.getSku();
        Date endDateTime = activityEntity.getEndDateTime();
        Long activityId = activityEntity.getActivityId();

        boolean status = activityDispatch.subtractionActivitySkuStock(sku, endDateTime);
        if (status) {
            // 库存扣减成功
            activityRepository.activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO.builder()
                    .sku(sku)
                    .activityId(activityId)
                    .build());
            return true;
        }

        throw new AppException(ResponseCode.ACTIVITY_SKU_STOCK_ERROR.getCode(), ResponseCode.ACTIVITY_SKU_STOCK_ERROR.getInfo());
    }
}
