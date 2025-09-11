package com.gzc.domain.award.service;

import com.gzc.domain.award.model.entity.DistributeAwardEntity;
import com.gzc.domain.award.model.entity.UserAwardRecordEntity;

public interface IAwardService {

    void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity);

    void distributeAward(DistributeAwardEntity distributeAward);
}
