package com.gzc.domain.rebate.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BehaviorVO {

    SIGN("sign", "签到返利"),
    PAY("pay","外部支付返利"),
    ;

    private final String code;
    private final String info;
}
