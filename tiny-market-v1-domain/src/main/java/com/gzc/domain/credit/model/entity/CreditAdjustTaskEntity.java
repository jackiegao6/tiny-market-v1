package com.gzc.domain.credit.model.entity;

import com.gzc.domain.credit.event.CreditAdjustSuccessMessageEvent;
import com.gzc.types.enums.MQTaskStateVO;
import com.gzc.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description 任务实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreditAdjustTaskEntity {

    /** 活动ID */
    private String userId;
    /** 消息主题 */
    private String topic;
    /** 消息编号 */
    private String messageId;
    /** 消息主体 */
    private BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustMessage> message;
    /** 任务状态；create-创建、completed-完成、fail-失败 */
    private MQTaskStateVO state;

}
