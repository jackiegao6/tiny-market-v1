package com.gzc.domain.task.service;

import com.gzc.domain.task.model.entity.UnHandledTaskEntity;
import com.gzc.domain.task.repository.ITaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService implements ITaskService{

    private final ITaskRepository taskRepository;

    @Override
    public List<UnHandledTaskEntity> queryUnHandledTaskEntity() {
        return taskRepository.queryUnHandledTaskEntity();
    }

    @Override
    public void sendMessage(UnHandledTaskEntity unHandledTaskEntity) {
        taskRepository.sendMessage(unHandledTaskEntity);

    }

    @Override
    public void updateTaskSendMessageCompleted(String userId, String messageId) {
        taskRepository.updateTaskSendMessageCompleted(userId, messageId);
    }

    @Override
    public void updateTaskSendMessageFail(String userId, String messageId) {
        taskRepository.updateTaskSendMessageFail(userId, messageId);
    }
}
