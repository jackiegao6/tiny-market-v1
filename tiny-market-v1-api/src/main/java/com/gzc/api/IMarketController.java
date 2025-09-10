package com.gzc.api;

import com.gzc.api.dto.market.RaffleStrategyRuleWeightRequestDTO;
import com.gzc.api.dto.market.RaffleStrategyRuleWeightResponseDTO;
import com.gzc.api.dto.market.UserActivityAccountRequestDTO;
import com.gzc.api.dto.market.UserActivityAccountResponseDTO;
import com.gzc.api.response.Response;

import java.util.List;

public interface IMarketController {

    Response<Boolean> calenderSignRebate(String userId);

    Response<Boolean> isUserCalenderSignRebate(String userId);

    Response<UserActivityAccountResponseDTO> queryUserActivityAccount(UserActivityAccountRequestDTO requestDTO);

    Response<List<RaffleStrategyRuleWeightResponseDTO>> queryUserRuleWeight(RaffleStrategyRuleWeightRequestDTO requestDTO);
}
