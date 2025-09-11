package com.gzc.domain.award.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description 账户状态枚举
 */
@Getter
@AllArgsConstructor
public enum AccountStatusVO {

    OPEN("open", "开启"),
    CLOSE("close", "冻结"),
    ;

    private final String code;
    private final String desc;

}
