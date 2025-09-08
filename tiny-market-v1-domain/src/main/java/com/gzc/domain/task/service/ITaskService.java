package com.gzc.domain.task.service;

import com.gzc.domain.task.model.entity.UnHandledTaskEntity;

import java.util.List;

public interface ITaskService {

    List<UnHandledTaskEntity> queryUnHandledTaskEntity();

    void sendMessage(UnHandledTaskEntity unHandledTaskEntity);

    void updateTaskSendMessageCompleted(String userId, String messageId);

    void updateTaskSendMessageFail(String userId, String messageId);


}
