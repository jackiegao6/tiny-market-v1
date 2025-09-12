package com.gzc.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RaffleAwardListRequestDTO implements Serializable {

    @Deprecated
    // 抽奖策略ID
    private Long strategyId;

    private Long activityId;

    private String userId;
}
