package com.gzc.domain.credit.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description 交易名称枚举值
 */
@Getter
@AllArgsConstructor
public enum TradeNameVO {

    REBATE("行为返利"),
    CONVERT_SKU("兑换抽奖"),
    CASH_RECHARGE("现金充值"),

    ;

    private final String name;

}
