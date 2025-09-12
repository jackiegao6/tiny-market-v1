package com.gzc.domain.activity.service.product;

import com.gzc.domain.activity.adapter.repository.IActivityRepository;
import com.gzc.domain.activity.model.entity.SkuProductEntity;
import com.gzc.domain.activity.service.IRaffleSkuProductService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RaffleSkuProductService implements IRaffleSkuProductService {

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public List<SkuProductEntity> querySkuProductEntityByActivityId(Long activityId) {
        return activityRepository.querySkuProductEntityByActivityId(activityId);
    }
}
