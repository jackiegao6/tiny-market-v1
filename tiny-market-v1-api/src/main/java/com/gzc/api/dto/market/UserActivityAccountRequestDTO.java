package com.gzc.api.dto.market;

import lombok.Data;

import java.io.Serializable;

/**
 * @description 用户活动账户请求对象
 */
@Data
public class UserActivityAccountRequestDTO implements Serializable {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动ID
     */
    private Long activityId;

}
