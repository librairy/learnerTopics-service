package org.librairy.service.learner.service;

import cc.mallet.topics.LDALauncher;
import cc.mallet.topics.LDAParameters;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import org.apache.avro.AvroRemoteException;
import org.librairy.service.modeler.facade.model.Dimension;
import org.librairy.service.modeler.facade.model.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class TopicsService {

    private static final Logger LOG = LoggerFactory.getLogger(TopicsService.class);

    @Value("#{environment['RESOURCE_FOLDER']?:'${resource.folder}'}")
    private String resourceFolder;

    @Autowired
    LDALauncher ldaLauncher;

    private ArrayList topics = new ArrayList();
    private Map<Integer, List<Element>> words = new HashMap<>();
    private LDAParameters parameters;

    @PostConstruct
    public void setup() throws Exception {
        if (ldaLauncher.existsModel(resourceFolder)) loadModel();
    }

    @PreDestroy
    public void destroy(){

    }

    public void remove(){
        ldaLauncher.removeModel(resourceFolder);
        topics = new ArrayList();
        words = new HashMap<>();
    }

    public void loadModel() throws Exception {
        LOG.info("Loading existing topic model");
        ParallelTopicModel topicModel   = ldaLauncher.getTopicModel(resourceFolder);
        parameters = ldaLauncher.readParameters(resourceFolder);

        try{
            this.topics = new ArrayList<>();
            this.words  = getTopWords(topicModel,50);


            IntStream.range(0,topicModel.getNumTopics()).forEach(id -> {

                Dimension topic = new Dimension();

                topic.setId(id);
                topic.setName((String)topicModel.getTopicAlphabet().lookupObject(id));

                topic.setDescription(words.get(id).stream().limit(10).map(w->w.getValue()).collect(Collectors.joining(",")));
                topics.add(topic);

            });

            LOG.info("Model load!");
        }catch (Exception e){
            LOG.warn("Error loading model: " + e.getMessage());
        }
    }

    public Map<Integer,List<Element>> getTopWords(ParallelTopicModel topicModel, int numWords) throws Exception {

        int numTopics = topicModel.getNumTopics();
        Alphabet alphabet = topicModel.getAlphabet();

        ArrayList<TreeSet<IDSorter>> topicSortedWords = topicModel.getSortedWords();

        Map<Integer,List<Element>> result = new HashMap<>();

        for (int topic = 0; topic < numTopics; topic++) {

            TreeSet<IDSorter> sortedWords = topicSortedWords.get(topic);

            if (sortedWords.isEmpty()){
                result.put(topic,Collections.emptyList());
                continue;
            }

            Double totalWeight = sortedWords.stream().map(w -> w.getWeight()).reduce((w1, w2) -> w1 + w2).get();

            // How many words should we report? Some topics may have fewer than
            //  the default number of words with non-zero weight.
            int limit = numWords;
            if (sortedWords.size() < numWords) { limit = sortedWords.size(); }

            List<Element> words = new ArrayList<>();

            Iterator<IDSorter> iterator = sortedWords.iterator();
            for (int i=0; i < limit; i++) {
                IDSorter info = iterator.next();
                words.add(new Element(String.valueOf(alphabet.lookupObject(info.getID())),info.getWeight()/totalWeight));
            }
            result.put(topic,words);
        }

        return result;
    }

    public List<Dimension> getTopics() throws AvroRemoteException {
        return topics;
    }

    public List<Element> getWords(int topicId, int maxWords) throws AvroRemoteException {
        if (!words.containsKey(topicId)) return Collections.emptyList();
        return words.get(topicId).stream().limit(maxWords).collect(Collectors.toList());

    }

    public LDAParameters getParameters() {
        return parameters;
    }
}
