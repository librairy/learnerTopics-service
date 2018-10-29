package org.librairy.service.learner.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.assertj.core.util.Strings;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.librairy.service.learner.Application;
import org.librairy.service.learner.facade.rest.model.Document;
import org.librairy.service.learner.facade.rest.model.ModelParameters;
import org.librairy.service.learner.facade.rest.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class TopicsRestIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(TopicsRestIntTest.class);

    @Autowired
    LearnerRestTopicsController service;


    static{
        Unirest.setDefaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
        Unirest.setDefaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
//        jacksonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        jacksonObjectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        Unirest.setObjectMapper(new ObjectMapper() {

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    @Test
    public void postTopics() throws UnirestException {

        ModelParameters req = new ModelParameters(new HashMap());

        ResponseEntity<Result> result = service.train(req);

        Assert.assertFalse(Strings.isNullOrEmpty(result.getBody().getResult()));


    }


    @Test
    public void addDocuments() throws UnirestException {


        List<String> texts = Arrays.asList(new String[]{
                "3 of Hearts is the self-titled debut studio album by the American group 3 of Hearts, released on March 6, 2001, through the record label RCA Nashville. It is a teen pop and country music album, though according to some music critics, it leans more towards pop music. The album was managed by American producer Byron Gallimore; its marketing focused on the group's crossover appeal to teenagers and young adults. 3 of Hearts performed on a national tour sponsored by Seventeen magazine and another sponsored by Walmart, and the singers were featured in several marketing campaigns. Reviews of the album were mixed; some critics praised the group's vocals and public image, but others criticized the songs as generic and lacking an authentic country sound.",
                "The Shape of Water is a 2017 American fantasy drama film directed by Guillermo del Toro and written by del Toro and Vanessa Taylor.[3][4] It stars Sally Hawkins, Michael Shannon, Richard Jenkins, Doug Jones, Michael Stuhlbarg, and Octavia Spencer. Set in Baltimore in 1962, the plot follows a mute custodian at a high-security government laboratory who falls in love with a captured human-amphibian creature.",
                "Guillermo del Toro Gómez is a Mexican film director, screenwriter, producer, and novelist. In his filmmaking career, del Toro has alternated between Spanish-language dark fantasy pieces, such as the gothic horror films The Devil's Backbone (2001) and Pan's Labyrinth (2006), and more mainstream American action films, such as the vampire superhero action film Blade II (2002), the supernatural superhero film Hellboy (2004), its sequel Hellboy II: The Golden Army (2008), Trollhunters (2016) and the science fiction monster film Pacific Rim (2013). His 2017 fantasy film The Shape of Water received critical acclaim and won a Golden Lion at the 74th Venice International Film Festival as well as the Academy Award for Best Picture. Del Toro received an Academy Award for Best Director for the film, as well as the Golden Globe, BAFTA, Critics' Choice, and Directors Guild of America.",
                "Blade II is a 2002 American superhero film based on the fictional character of the same name from Marvel Comics. It is the sequel of the first film and the second part of the Blade film series, followed by Blade: Trinity. It was written by David S. Goyer, who also wrote the previous film, directed by Guillermo del Toro, and had Wesley Snipes returning as the lead character and producer.",
                "Blade: Trinity (also known as Blade III or Blade III: Trinity) is a 2004 American superhero film written, produced and directed by David S. Goyer, who also wrote the screenplays to Blade and Blade II. It stars Wesley Snipes, who also produced, in the title role based on the Marvel Comics character Blade alongside Ryan Reynolds, Jessica Biel, Kris Kristofferson, Dominic Purcell, Parker Posey and Triple H."
        });

        AtomicInteger counter = new AtomicInteger();
        for(String text: texts){

            Document document = new Document(Document.newBuilder().setId(String.valueOf(text.hashCode())).setName("doc-"+counter.incrementAndGet()).setText(text).build());

            HttpResponse<JsonNode> res = Unirest
                    //.post("http://lab4.librairy.linkeddata.es/jgalan-topics/documents")
                    .post("http://localhost:7777/documents")
                    //.basicAuth("jgalan", "oeg2018")
                    .basicAuth("librairy", "l1brA1ry")
                    .body(document).asJson();
            LOG.info("Response: " + res.getStatus());

        }



    }
}