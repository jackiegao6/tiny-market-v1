package com.gzc.api;

import com.gzc.api.response.Response;

public interface IMarketController {

    Response<Boolean> calenderSignRebate(String userId);
}
