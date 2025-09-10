package com.gzc.domain.rebate.model.entity;

import com.gzc.domain.rebate.event.SendRebateMessageEvent;
import com.gzc.domain.rebate.model.valobj.TaskStateVO;
import com.gzc.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BehaviorRebateTaskEntity {

    private String userId;
    private String topic;
    private String messageId;
    private BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> message;
    private TaskStateVO taskStateVO;
}
