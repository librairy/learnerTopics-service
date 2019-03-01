package org.librairy.service.learner.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class StopwordService {

    private static final Logger LOG = LoggerFactory.getLogger(StopwordService.class);

    private static final Integer TOP    = 5;

    private static final Double RATIO   = 0.5;

    public List<String> detect(Map<String,List<String>> topics){

        int numTopics = 0;
        Map<String,Integer> wordsFreq = new HashMap<>();
        Map<String,Double> wordsTF = new HashMap<>();

        for(Map.Entry<String,List<String>> topic: topics.entrySet()){

            numTopics++;

            List<String> doc = topic.getValue();

            if (doc.isEmpty()) continue;

            doc.stream().limit(TOP).forEach(w -> {
                if (!wordsFreq.containsKey(w)){
                    wordsFreq.put(w,0);
                }
                wordsFreq.put(w, wordsFreq.get(w)+1);

                if (!wordsTF.containsKey(w)){
                    wordsTF.put(w,0.0);
                }
                wordsTF.put(w,wordsTF.get(w)+tf(doc,w));
            });

        }


        final int size = numTopics;
        Map<String,Double> wordsIDF = new HashMap<>();
        wordsFreq.entrySet().forEach(e -> wordsIDF.put(e.getKey(), idf(size, e.getValue())));

        Map<String,Double> wordsTFIDF = new HashMap<>();
        wordsFreq.entrySet().forEach(e -> wordsTFIDF.put(e.getKey(), wordsTF.get(e.getKey())*wordsIDF.get(e.getKey())));

        List<String> stopwords = wordsIDF.entrySet().stream().filter(e -> e.getValue() < RATIO).sorted((a, b) -> a.getValue().compareTo(b.getValue())).map(e -> e.getKey()).collect(Collectors.toList());

        wordsTFIDF.entrySet().stream().sorted((a,b) -> -a.getValue().compareTo(b.getValue())).limit(10).forEach(e -> LOG.info("WordTFIDF: " + e.getKey() + " = " + e.getValue()));

        LOG.info("######");

        wordsIDF.entrySet().stream().sorted((a,b) -> a.getValue().compareTo(b.getValue())).limit(10).forEach(e -> LOG.info("WordIDF: " + e.getKey() + " = " + e.getValue()));

        LOG.info("######");

        wordsTF.entrySet().stream().sorted((a,b) -> -a.getValue().compareTo(b.getValue())).limit(10).forEach(e -> LOG.info("WordTF: " + e.getKey() + " = " + e.getValue()));

        return stopwords;
    }


    private double idf(int size, int freq){
        return Math.log(size) / freq;
    }

    private double tf(List<String> words, String w){
        int sum = IntStream.range(1,words.size()+1).reduce((a,b) -> a+b).getAsInt();
        return (words.size() - words.indexOf(w)) / Double.valueOf(sum);
    }

}
