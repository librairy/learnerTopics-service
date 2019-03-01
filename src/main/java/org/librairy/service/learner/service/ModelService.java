package org.librairy.service.learner.service;

import cc.mallet.topics.ModelFactory;
import cc.mallet.topics.ModelParams;
import org.librairy.service.learner.builders.CorpusBuilder;
import org.librairy.service.learner.facade.model.TopicsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class ModelService {

    private static final Logger LOG = LoggerFactory.getLogger(ModelService.class);

    @Value("#{environment['RESOURCE_FOLDER']?:'${resource.folder}'}")
    String resourceFolder;

    @Autowired
    ModelFactory modelFactory;

    @Autowired
    MailService mailService;

    public Boolean train(CorpusBuilder corpus, TopicsRequest request) {
        Map<String,String> parameters = request.getParameters() != null? request.getParameters() : new HashMap<>();

        try {
            if (corpus.getNumDocs() <= 0 ){
                LOG.info("Corpus is empty.");
                mailService.notifyModelError(request, "Model not created. Corpus is empty.");
                return false;
            }

            LOG.info("ready to create a new topic model with parameters: " + parameters);
            ModelParams ldaParameters = new ModelParams(corpus.getFilePath().toFile().getAbsolutePath(), resourceFolder);

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
            if (parameters.containsKey("multigrams"))   ldaParameters.setEntities(Boolean.valueOf(parameters.get("multigrams")));
            if (parameters.containsKey("entities"))     ldaParameters.setEntities(Boolean.valueOf(parameters.get("entities")));
            if (parameters.containsKey("seed"))         ldaParameters.setSeed(Integer.valueOf(parameters.get("seed")));
            if (parameters.containsKey("stoplabels"))   ldaParameters.setStoplabels(Arrays.asList(parameters.get("stoplabels").split(" ")));

            ldaParameters.setSize(corpus.getNumDocs());

            if (!parameters.containsKey("algorithm")){
                List<String> labels = request.getFrom().getFields().getLabels();
                if (labels != null && !labels.isEmpty()) parameters.put("algorithm","llda");
            }

            modelFactory.train(parameters,ldaParameters);
            return true;
        } catch (IOException e) {
            LOG.error("Error building a topic model from: " + parameters, e);
            mailService.notifyModelError(request, "Model not created. For details consult your administrator. ");
            return false;
        } catch(ClassCastException e) {
            LOG.error("Error reading parameters from: " + parameters, e);
            mailService.notifyModelError(request, "Model not created. For details consult your administrator. ");
            return false;
        } catch(Exception e){
            LOG.error("Unexpected error during training phase", e);
            mailService.notifyModelError(request, "Model not created. For details consult your administrator. ");
            return false;
        }
    }
}
