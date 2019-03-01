package org.librairy.service.learner.model;

import cc.mallet.pipe.Pipe;
import cc.mallet.topics.ParallelTopicModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicReport {

    private static final Logger LOG = LoggerFactory.getLogger(TopicReport.class);

    ParallelTopicModel model;

    Pipe pipe;


    public TopicReport() {
    }

    public boolean isEmpty(){
        return model == null || pipe == null;
    }

    public TopicReport(ParallelTopicModel model, Pipe pipe) {
        this.model = model;
        this.pipe = pipe;
    }

    public ParallelTopicModel getModel() {
        return model;
    }

    public Pipe getPipe() {
        return pipe;
    }
}
