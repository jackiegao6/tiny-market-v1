package com.gzc.domain.rebate.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStateVO {

    CREATE("create", "创建"),
    COMPLETE("complete", "发送完成"),
    FAIL("fail", "发送失败"),
    ;

    private final String code;
    private final String desc;

}