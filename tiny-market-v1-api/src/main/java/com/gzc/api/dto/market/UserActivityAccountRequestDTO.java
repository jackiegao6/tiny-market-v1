package com.gzc.api.dto.market;

import lombok.Data;

/**
 * @description 用户活动账户请求对象
 */
@Data
public class UserActivityAccountRequestDTO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动ID
     */
    private Long activityId;

}
