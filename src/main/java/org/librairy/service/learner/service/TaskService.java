package org.librairy.service.learner.service;

import org.librairy.service.learner.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

@Component
public class TaskService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    QueueService queueService;

    @Autowired
    TopicService topicsService;

    @Autowired
    AnnotationService annotationService;

    private ExecutorService executors;

    @PostConstruct
    public void setup(){
        executors = Executors.newSingleThreadExecutor();
        executors.submit(new Runnable() {
            @Override
            public void run() {

                while (true){
                    Task task;
                    try{
                        LOG.info("Waiting for tasks ...");
                        task = queueService.take();


                        switch (task.getType()){
                            case TOPICS:
                                topicsService.create(task.getTopicsRequest());
                                break;
                            case ANNOTATIONS:
                                annotationService.create(task.getAnnotationRequest());
                                break;
                            default: LOG.warn("Task type not found: " + task);
                        }

                    }catch (Exception e){
                        LOG.error("Error handling a task",e);
                    }
                }

            }
        });
    }
}
