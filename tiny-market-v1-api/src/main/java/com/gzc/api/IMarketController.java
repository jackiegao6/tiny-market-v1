package com.gzc.api;

import com.gzc.api.dto.market.*;
import com.gzc.api.response.Response;

import java.math.BigDecimal;
import java.util.List;

public interface IMarketController {

    Response<Boolean> calenderSignRebate(String userId);

    Response<Boolean> isUserCalenderSignRebate(String userId);

    Response<UserActivityAccountResponseDTO> queryUserActivityAccount(UserActivityAccountRequestDTO requestDTO);

    Response<List<RaffleStrategyRuleWeightResponseDTO>> queryUserRuleWeight(RaffleStrategyRuleWeightRequestDTO requestDTO);

    Response<BigDecimal> queryCreditAccount(String userId);

    Response<List<SkuProductResponseDTO>> querySkuListByActivityId(Long activityId);

    Response<Boolean> creditExchangeSku(SkuProductShopCartRequestDTO requestDTO);
}
