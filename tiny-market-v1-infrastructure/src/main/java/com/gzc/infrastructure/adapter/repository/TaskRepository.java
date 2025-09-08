package com.gzc.infrastructure.adapter.repository;

import com.gzc.domain.task.model.entity.UnHandledTaskEntity;
import com.gzc.domain.task.repository.ITaskRepository;
import com.gzc.infrastructure.dao.ITaskDao;
import com.gzc.infrastructure.dao.po.Task;
import com.gzc.infrastructure.event.EventPublisher;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TaskRepository implements ITaskRepository {

    @Resource
    private ITaskDao taskDao;
    @Resource
    private EventPublisher eventPublisher;


    @Override
    public List<UnHandledTaskEntity> queryUnHandledTaskEntity() {
        List<Task> tasks = taskDao.queryNoSendMessageTaskList();
        List<UnHandledTaskEntity> taskEntities = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            UnHandledTaskEntity taskEntity = new UnHandledTaskEntity();
            taskEntity.setUserId(task.getUserId());
            taskEntity.setTopic(task.getTopic());
            taskEntity.setMessageId(task.getMessageId());
            taskEntity.setMessage(task.getMessage());
            taskEntities.add(taskEntity);
        }
        return taskEntities;
    }

    @Override
    public void sendMessage(UnHandledTaskEntity unHandledTaskEntity) {
        eventPublisher.publish(unHandledTaskEntity.getTopic(), unHandledTaskEntity.getMessage());
    }

    @Override
    public void updateTaskSendMessageCompleted(String userId, String messageId) {
        Task taskReq = new Task();
        taskReq.setUserId(userId);
        taskReq.setMessageId(messageId);
        taskDao.updateTaskSendMessageCompleted(taskReq);
    }

    @Override
    public void updateTaskSendMessageFail(String userId, String messageId) {
        Task taskReq = new Task();
        taskReq.setUserId(userId);
        taskReq.setMessageId(messageId);
        taskDao.updateTaskSendMessageFail(taskReq);
    }
}
