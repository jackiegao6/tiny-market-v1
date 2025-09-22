package com.gzc.domain.rebate.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 行为返利类型：签到返利、支付返利
 */
@Getter
@AllArgsConstructor
public enum BehaviorTypeVO {

    SIGN("sign", "签到返利"),
    PAY("pay","外部支付返利"),
    ;

    private final String code;
    private final String info;
}
