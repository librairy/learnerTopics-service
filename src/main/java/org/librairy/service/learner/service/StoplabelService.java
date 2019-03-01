package org.librairy.service.learner.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class StoplabelService {

    private static final Logger LOG = LoggerFactory.getLogger(StoplabelService.class);

    public List<String> detect(Map<String,List<String>> topics, int topWords){

        List<String> stopLabels = new ArrayList<>();

        for(Map.Entry<String,List<String>> topic : topics.entrySet()){

            String name = topic.getKey();

            if (topic.getValue().isEmpty() || topic.getValue().size() < topWords) {
                stopLabels.add(String.valueOf(name));
            }

        }

        return  stopLabels;

    }

}
