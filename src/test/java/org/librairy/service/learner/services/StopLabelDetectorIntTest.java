package org.librairy.service.learner.services;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.assertj.core.util.Strings;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class StopLabelDetectorIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(StopLabelDetectorIntTest.class);

    @Test
    public void execute() throws UnirestException {

        String endpoint = "http://localhost:8080";

        HttpResponse<JsonNode> response = Unirest.get(endpoint + "/topics").asJson();

        JSONArray topics = response.getBody().getArray();

        int numTopics = 0;
        List<String> stopLabels = new ArrayList<>();

        for(int i=0;i<topics.length();i++){

            JSONObject topic = topics.getJSONObject(i);

            numTopics++;
            int name = topic.getInt("id");
            String description = topic.getString("description");

            if (Strings.isNullOrEmpty(description)) {
                stopLabels.add(String.valueOf(name));
                continue;
            }

            if (Arrays.stream(description.split(",")).collect(Collectors.toList()).size() <10){
                stopLabels.add(String.valueOf(name));
                continue;
            }

        }

        LOG.info("Num Topics: " + numTopics);
        LOG.info("Num Stoplabels: " + stopLabels.size());
        LOG.info("Stoplabels: "  + stopLabels.stream().collect(Collectors.joining(", ")));


    }

}
