package com.gzc.domain.award.adapter.repository;

import com.gzc.domain.award.model.aggregate.UserAwardRecordAggregate;

public interface IAwardRepository {
    void saveUserAwardRecord(UserAwardRecordAggregate aggregate);

}
