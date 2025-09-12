package com.gzc.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @description 抽奖请求参数
 */
@Data
public class RaffleRequestDTO implements Serializable {

    // 抽奖策略ID
    private Long strategyId;

}
