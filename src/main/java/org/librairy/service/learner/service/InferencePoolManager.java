package org.librairy.service.learner.service;

import cc.mallet.topics.LDALauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class InferencePoolManager {

    private static final Logger LOG = LoggerFactory.getLogger(InferencePoolManager.class);

    @Value("#{environment['NLP_ENDPOINT']?:'${nlp.endpoint}'}")
    String nlpEndpoint;

    @Value("#{environment['RESOURCE_FOLDER']?:'${resource.folder}'}")
    String resourceFolder;

    @Autowired
    LDALauncher ldaLauncher;

    @Autowired
    LibrairyNlpClient client;

    Map<String,Inferencer> inferencePool;

    @PostConstruct
    public void setup(){
        inferencePool     = new ConcurrentHashMap<>();
    }

    public Inferencer get(Thread thread, String language) throws Exception {

        String id = "thread"+thread.getId();
        if (!inferencePool.containsKey(id)){
            LOG.info("Initializing Topic Inferencer for thread: " + id);
            Inferencer inferencer = new Inferencer(ldaLauncher,client,language,resourceFolder);
            inferencePool.put(id,inferencer);
        }
        return inferencePool.get(id);

    }

    public void update(){
        this.inferencePool.clear();
    }
}
