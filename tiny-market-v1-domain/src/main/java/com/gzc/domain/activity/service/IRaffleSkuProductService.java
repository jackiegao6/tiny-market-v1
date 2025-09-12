package com.gzc.domain.activity.service;

import com.gzc.domain.activity.model.entity.SkuProductEntity;

import java.util.List;

public interface IRaffleSkuProductService {

    List<SkuProductEntity> querySkuProductEntityByActivityId(Long activityId);
}
