package com.gzc.api.dto;

import lombok.Data;

@Data
public class RaffleAwardListRequestDTO {

    @Deprecated
    // 抽奖策略ID
    private Long strategyId;

    private Long activityId;

    private String userId;
}
