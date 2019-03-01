package org.librairy.service.learner.services;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.assertj.core.util.Strings;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.librairy.service.learner.service.StopwordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class StopWordDetectorIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(StopWordDetectorIntTest.class);

    @Test
    public void execute() throws UnirestException {

        String endpoint = "http://localhost:8080";

        HttpResponse<JsonNode> response = Unirest.get(endpoint + "/topics").asJson();

        JSONArray topics = response.getBody().getArray();

        int numTopics = 0;

        List<String> numWords = new ArrayList();
        List<String> minWords = new ArrayList();

        for(int i=0;i<topics.length();i++){

            numTopics++;
            JSONObject topic = topics.getJSONObject(i);

            String description = topic.getString("description");

            if (Strings.isNullOrEmpty(description)) continue;

            numWords.addAll(Arrays.stream(description.split(",")).filter(w -> w.matches(".*\\d+.*")).collect(Collectors.toList()));
            minWords.addAll(Arrays.stream(description.split(",")).filter(w -> w.length()<3).collect(Collectors.toList()));

        }


        LOG.info("Num Words: " + numWords);
        LOG.info("Min Words: " + minWords);

    }

    @Test
    public void analyze() throws UnirestException {

        String endpoint = "http://localhost:8080";

        HttpResponse<JsonNode> response = Unirest.get(endpoint + "/topics").asJson();

        JSONArray topics = response.getBody().getArray();

        int numTopics = 0;

        List<String> numWords = new ArrayList();
        List<String> minWords = new ArrayList();

        StopwordService service = new StopwordService();

        Map<String,List<String>> topicMap = new HashMap<>();
        for(int i=0;i<topics.length();i++){

            numTopics++;
            JSONObject topic = topics.getJSONObject(i);

            String description = topic.getString("description");


            topicMap.put(topic.getString("name"),Arrays.stream(description.split(",")).collect(Collectors.toList()));

        }

        List<String> sw = service.detect(topicMap);

        LOG.info("Stop Words: " + sw);

    }



}
