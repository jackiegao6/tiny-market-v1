package com.gzc.domain.task.repository;

import com.gzc.domain.task.model.entity.UnHandledTaskEntity;

import java.util.List;

public interface ITaskRepository {

    List<UnHandledTaskEntity> queryUnHandledTaskEntity();

    void sendMessage(UnHandledTaskEntity unHandledTaskEntity);

    void updateTaskSendMessageCompleted(String userId, String messageId);

    void updateTaskSendMessageFail(String userId, String messageId);

}
