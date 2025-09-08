package com.gzc.domain.activity.service;

import com.gzc.domain.activity.adapter.repository.IActivityRepository;
import org.springframework.stereotype.Service;

/**
 * @description 抽奖活动服务
 */
@Service
public class RaffleActivityService extends AbstractRaffleActivity {

    public RaffleActivityService(IActivityRepository activityRepository) {
        super(activityRepository);
    }

}
