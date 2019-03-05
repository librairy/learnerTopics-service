package org.librairy.service.learner.service;

import org.librairy.service.learner.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

@Component
public class QueueService {

    private static final Logger LOG = LoggerFactory.getLogger(QueueService.class);

    private final LinkedBlockingQueue<Task> taskQueue;


    public QueueService() {
        this.taskQueue        = new LinkedBlockingQueue<Task>();
    }

    public void add(Task task) throws InterruptedException {
        LOG.info("task added to queue: " + task);
        this.taskQueue.put(task);
    }

    public Task take() throws InterruptedException {
        Task task = this.taskQueue.take();
        LOG.info("task took from queue: " + task);
        return task;
    }
}
