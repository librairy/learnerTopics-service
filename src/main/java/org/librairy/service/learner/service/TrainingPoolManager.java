package org.librairy.service.learner.service;

import cc.mallet.topics.ModelFactory;
import cc.mallet.topics.ModelParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
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
    ModelFactory modelFactory;

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

            try {
                ModelParams ldaParameters = new ModelParams(corpusService.getFilePath().toFile().getAbsolutePath(), resourceFolder);

                if (parameters.containsKey("alpha"))        ldaParameters.setAlpha(Double.valueOf(parameters.get("alpha")));
                if (parameters.containsKey("beta"))         ldaParameters.setBeta(Double.valueOf(parameters.get("beta")));
                if (parameters.containsKey("topics"))       ldaParameters.setNumTopics(Integer.valueOf(parameters.get("topics")));
                if (parameters.containsKey("iterations"))   ldaParameters.setNumIterations(Integer.valueOf(parameters.get("iterations")));
                if (parameters.containsKey("language"))     ldaParameters.setLanguage(parameters.get("language"));
                if (parameters.containsKey("pos"))          ldaParameters.setPos(parameters.get("pos"));
                if (parameters.containsKey("retries"))      ldaParameters.setNumRetries(Integer.valueOf(parameters.get("retries")));
                if (parameters.containsKey("topwords"))     ldaParameters.setNumTopWords(Integer.valueOf(parameters.get("topwords")));
                if (parameters.containsKey("stopwords"))    ldaParameters.setStopwords(Arrays.asList(parameters.get("stopwords").split(" ")));
                if (parameters.containsKey("minfreq"))      ldaParameters.setMinFreq(Integer.valueOf(parameters.get("minfreq")));
                if (parameters.containsKey("maxdocratio"))  ldaParameters.setMaxDocRatio(Double.valueOf(parameters.get("maxdocratio")));
                if (parameters.containsKey("raw"))          ldaParameters.setRaw(Boolean.valueOf(parameters.get("raw")));
                if (parameters.containsKey("inference"))    ldaParameters.setInference(Boolean.valueOf(parameters.get("inference")));


                modelFactory.train(parameters,ldaParameters);
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
