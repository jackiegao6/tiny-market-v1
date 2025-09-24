package com.gzc.api;


import com.gzc.api.dto.RaffleAwardListRequestDTO;
import com.gzc.api.dto.RaffleAwardListResponseDTO;
import com.gzc.api.response.Response;

import java.util.List;

/**
 * @description 抽奖服务支持接口
 */
public interface IBeforeRaffleController {

    /**
     * 活动装配，数据预热缓存
     *
     * @param activityId 活动ID
     * @return 装配结果
     */
    Response<Boolean> armory(Long activityId);

    /**
     * 查询抽奖奖品列表配置
     *
     * @param requestDTO 用户id、活动id
     * @return 奖品列表数据
     */
    Response<List<RaffleAwardListResponseDTO>> queryRaffleAwardList(RaffleAwardListRequestDTO requestDTO);

    /**
     * 查询用户累计的抽奖次数
     *
     * @param userId 用户id
     * @return 累计的抽奖次数
     */
    Response<Integer> queryUserTotalCount(String userId);

}
