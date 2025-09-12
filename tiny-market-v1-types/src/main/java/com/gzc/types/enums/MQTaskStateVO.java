package com.gzc.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description mq消息 状态值对象
 */
@Getter
@AllArgsConstructor
public enum MQTaskStateVO {

    create("create", "创建"),
    complete("complete", "发送完成"),
    fail("fail", "发送失败"),
    ;

    private final String code;
    private final String desc;

}
