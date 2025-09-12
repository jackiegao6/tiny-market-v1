package com.gzc.api;

import com.gzc.api.response.Response;

public interface IDCCValueManageController {

    Response<Boolean> updateDCCValue(String key, String value);
}
