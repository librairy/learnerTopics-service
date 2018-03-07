package org.librairy.service.learner.service;

import org.apache.avro.AvroRemoteException;
import org.librairy.service.modeler.facade.model.ModelerService;
import org.librairy.service.modeler.facade.model.Topic;
import org.librairy.service.modeler.facade.model.TopicDistribution;
import org.librairy.service.modeler.facade.model.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ModelerServiceImpl implements ModelerService {

    private static final Logger LOG = LoggerFactory.getLogger(ModelerServiceImpl.class);

    @Value("#{environment['RESOURCE_FOLDER']?:'${resource.folder}'}")
    String resourceFolder;

    String model              ;

    @Autowired
    CorpusService corpusService;

    @Autowired
    TopicsService topicsService;

    @Autowired
    InferencePoolManager inferencePoolManager;

    @Autowired
    TrainingPoolManager trainingPoolManager;

    @PostConstruct
    public void setup() throws IOException {

        //// Load resources
        //model              = Paths.get(resourceFolder,"resource.bin").toFile().getAbsolutePath();

        LOG.info("Service initialized");
    }

    @Override
    public List<Topic> topics() throws AvroRemoteException {
        return topicsService.getTopics();
    }

    @Override
    public List<Word> words(int topicId, int maxWords) throws AvroRemoteException {
        return topicsService.getWords(topicId,maxWords);
    }

    @Override
    public List<TopicDistribution> inference(String s) throws AvroRemoteException {
        try {

            List<Double> shape = shape(s);
            List<Topic> topics = topics();

            List<TopicDistribution> topicDistributions = new ArrayList<>();
            for(int i=0;i<topics.size();i++){
                TopicDistribution topicDistribution = new TopicDistribution();
                topicDistribution.setTopic(topics.get(i));
                topicDistribution.setScore(shape.get(i));
                topicDistributions.add(topicDistribution);
            }
            return topicDistributions;
        } catch (Exception e) {
            throw new AvroRemoteException("Error loading topic model",e);
        }
    }

    @Override
    public List<Double> shape(String s) throws AvroRemoteException {
        try {

            String language    = topicsService.getParameters().getLanguage();
            List<Double> shape = inferencePoolManager.get(Thread.currentThread(), language).inference(s);
            return shape;
        } catch (Exception e) {
            throw new AvroRemoteException("Error loading topic model",e);
        }
    }
}
