package com.gzc.domain.activity.service.rule;

import com.gzc.domain.activity.model.entity.ActivityCountEntity;
import com.gzc.domain.activity.model.entity.ActivityEntity;
import com.gzc.domain.activity.model.entity.ActivitySkuEntity;

public interface IActionChain extends IActionChainArmory {

    Boolean logic(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);

}
