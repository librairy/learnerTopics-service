package org.librairy.service.learner.model;

import org.librairy.service.learner.facade.rest.model.TopicsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Task {

    private static final Logger LOG = LoggerFactory.getLogger(Task.class);

    private TopicsRequest topicsRequest;
    private AnnotationRequest annotationRequest;

    public enum Type {
        TOPICS, ANNOTATIONS
    }

    private final Type type;

    public Task(TopicsRequest topicsRequest) {
        this.type = Type.TOPICS;
        this.topicsRequest = topicsRequest;
    }

    public Task(AnnotationRequest annotationRequest) {
        this.type = Type.ANNOTATIONS;
        this.annotationRequest = annotationRequest;
    }

    public Type getType() {
        return type;
    }

    public TopicsRequest getTopicsRequest() {
        return topicsRequest;
    }

    public AnnotationRequest getAnnotationRequest() {
        return annotationRequest;
    }

    @Override
    public String toString() {
        return "Task{" +
                "type=" + type +
                ", topicsRequest=" + topicsRequest +
                "annotationRequest=" + annotationRequest +
                '}';
    }
}
