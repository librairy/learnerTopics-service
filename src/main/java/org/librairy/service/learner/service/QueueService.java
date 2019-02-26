package org.librairy.service.learner.service;

import org.librairy.service.learner.facade.model.TopicsRequest;
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

    private final LinkedBlockingQueue<String> cacheQueue;
    private final LinkedBlockingQueue<TopicsRequest> topicsQueue;


    public QueueService() {
        this.topicsQueue    = new LinkedBlockingQueue<TopicsRequest>();
        this.cacheQueue     = new LinkedBlockingQueue<String>();
    }

    public void addTopicsRequest(TopicsRequest request) throws InterruptedException {
        LOG.info("workspace request added to queue: " + request);
        this.topicsQueue.put(request);
    }

    public void addCacheRequest(String request) throws InterruptedException {
        LOG.info("cache request added to queue: " + request);
        this.cacheQueue.put(request);
    }

    public TopicsRequest getTopicsRequest() throws InterruptedException {
        TopicsRequest request = this.topicsQueue.take();
        LOG.info("workspace request took from queue: " + request);
        return request;
    }

    public String getCacheRequest() throws InterruptedException {
        String request = this.cacheQueue.take();
        LOG.info("cache request took from queue: " + request);
        return request;
    }
}
