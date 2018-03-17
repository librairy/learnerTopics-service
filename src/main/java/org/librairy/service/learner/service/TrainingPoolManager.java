package org.librairy.service.learner.service;

import cc.mallet.topics.LDALauncher;
import cc.mallet.topics.LDAParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class TrainingPoolManager {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingPoolManager.class);

    @Value("#{environment['RESOURCE_FOLDER']?:'${resource.folder}'}")
    String resourceFolder;

    @Autowired
    LDALauncher ldaLauncher;

    @Autowired
    CorpusService corpusService;

    Boolean isTraining;

    private ExecutorService executors;


    @PostConstruct
    public void setup(){
        executors = Executors.newSingleThreadExecutor();
        isTraining = false;
    }

    public Boolean train(Map<String,String> parameters) {

        if (isTraining) return false;

        executors.submit(() -> {
            LOG.info("ready to create a new topic model from: " + parameters);
            isTraining = true;
            LDAParameters ldaParameters = new LDAParameters(corpusService.getFilePath().toFile().getAbsolutePath(), resourceFolder);

            try {
                if (parameters.containsKey("alpha")) ldaParameters.setAlpha(Double.valueOf(parameters.get("alpha")));
                if (parameters.containsKey("beta")) ldaParameters.setBeta(Double.valueOf(parameters.get("beta")));
                if (parameters.containsKey("topics")) ldaParameters.setNumTopics(Integer.valueOf(parameters.get("topics")));
                if (parameters.containsKey("iterations")) ldaParameters.setNumIterations(Integer.valueOf(parameters.get("iterations")));
                if (parameters.containsKey("language")) ldaParameters.setLanguage(parameters.get("language"));

                ldaLauncher.train(ldaParameters);
            } catch (IOException e) {
                LOG.error("Error building a topic model from: " + parameters, e);
            } catch(ClassCastException e) {
                LOG.error("Error reading parameters from: " + parameters, e);
            } catch(Exception e){
                LOG.error("Unexpected error during training phase", e);
            }finally {
                isTraining = false;
            }

        });

        return true;
    }

}
