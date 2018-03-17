package org.librairy.service.learner.service;

import org.apache.avro.AvroRemoteException;
import org.librairy.service.nlp.facade.AvroClient;
import org.librairy.service.nlp.facade.model.Form;
import org.librairy.service.nlp.facade.model.PoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class LibrairyNlpClient {

    private static final Logger LOG = LoggerFactory.getLogger(LibrairyNlpClient.class);

    @Value("#{environment['NLP_ENDPOINT']?:'${nlp.endpoint}'}")
    String nlpEndpoint;


    Map<String,AvroClient> clients;

    @PostConstruct
    public void setup(){
        clients = new ConcurrentHashMap<>();
    }

    @PreDestroy
    public void shutdown(){
        if (!clients.isEmpty()){
            clients.entrySet().stream().forEach(entry -> entry.getValue().close());
            clients.clear();
        }
    }


    public String lemmatize(String text, String language, List<PoS> poSList){
        String key = "Thread-"+ Thread.currentThread().getId();
        if (!clients.containsKey(key)){
            AvroClient client = new AvroClient();
            String nlpServiceEndpoint = nlpEndpoint.replace("%%", language);
            try {
                LOG.info("Creating a new AVRO connection to: " + nlpServiceEndpoint);
                client.open(nlpServiceEndpoint,65111);
                clients.put(key,client);
            } catch (IOException e) {
                LOG.error("Error connecting to nlp service: " + nlpServiceEndpoint, e);
            }
        }
        AvroClient client = clients.get(key);
        try {
            return client.process(text,poSList, Form.LEMMA);
        } catch (AvroRemoteException e) {
            LOG.error("Error retrieving lemmas from nlp service: " + nlpEndpoint.replace("%%", language), e);
        }
        return "";
    }

}
