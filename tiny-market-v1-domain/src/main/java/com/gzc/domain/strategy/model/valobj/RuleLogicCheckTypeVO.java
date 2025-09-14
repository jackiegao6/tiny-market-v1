package com.gzc.domain.strategy.model.valobj;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description 规则过滤校验类型值对象
 */
@Getter
@AllArgsConstructor
public enum RuleLogicCheckTypeVO {

    ALLOW("0000", "放行；该奖品id符合当前规则要求"),
    TAKE_OVER("0001","接管；该奖品id不符合当前规则要求，一般返回兜底奖品"),
    ;

    private final String code;
    private final String info;

}
