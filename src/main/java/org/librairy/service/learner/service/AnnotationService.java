package org.librairy.service.learner.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.json.JSONArray;
import org.json.JSONObject;
import org.librairy.service.learner.executors.ParallelExecutor;
import org.librairy.service.learner.model.AnnotationRequest;
import org.librairy.service.modeler.facade.rest.model.ClassRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class AnnotationService {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationService.class);

    @Autowired
    QueueService queueService;

    @Autowired
    MailService mailService;

    private ExecutorService executors;

    static{
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

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

        Unirest.setTimeouts(20000, 120000);
        Unirest.setDefaultHeader("accept", "application/json");
        Unirest.setDefaultHeader("Content-Type", "application/json");

    }


    @PostConstruct
    public void setup(){
        executors = Executors.newSingleThreadExecutor();
        executors.submit(new Runnable() {
            @Override
            public void run() {

                while (true){
                    AnnotationRequest request = null;
                    try{
                        LOG.info("Waiting for annotation requests ...");
                        request = queueService.getAnnotationRequest();

                        annotate(request);

                    }catch (Exception e){
                        LOG.error("Error creating topics",e);
                        if (request != null) mailService.notifyAnnotationError(request, "Annotation error. For details consult with your system administrator.  ");
                    }

                }

            }
        });
    }


    public void annotate(AnnotationRequest annotationRequest){

        try{

            HttpSolrClient solrClient = new HttpSolrClient.Builder(annotationRequest.getCollection()).build();

            Integer window = 500;
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setRows(window);
            solrQuery.addSort("id", SolrQuery.ORDER.asc);

            Arrays.asList("id","txt_t").forEach(f -> solrQuery.addField(f));

            String query = (Strings.isNullOrEmpty(annotationRequest.getFilter()))? "*:*" : annotationRequest.getFilter();
            solrQuery.setQuery(query);

            String nextCursorMark   = CursorMarkParams.CURSOR_MARK_START;
            AtomicInteger index = new AtomicInteger();
            ParallelExecutor executor = new ParallelExecutor();
            while(true){

                String cursorMark       = nextCursorMark;
                solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);


                QueryResponse rsp = solrClient.query(solrQuery);
                nextCursorMark = rsp.getNextCursorMark();

                SolrDocumentList results = rsp.getResults();

                for(SolrDocument solrDoc : results){

                    executor.submit(() -> {
                        try{
                            String id   = (String) solrDoc.get("id");
                            String txt  = (String) solrDoc.get("txt_t");

                            if (Strings.isNullOrEmpty(txt)) return;

                            Map<Integer, List<String>> topicsMap = new HashMap<>();

                            ClassRequest request = new ClassRequest();
                            request.setText(txt);
                            HttpResponse<JsonNode> response = Unirest
                                    .post(annotationRequest.getModel() + "/classes")
                                    .body(request).asJson();
                            JSONArray topics = response.getBody().getArray();

                            for(int i=0;i<topics.length();i++){
                                JSONObject topic = topics.getJSONObject(i);
                                Integer level   = topic.getInt("id");
                                String tId      = topic.getString("name");

                                if (!topicsMap.containsKey(level)) topicsMap.put(level,new ArrayList<>());

                                topicsMap.get(level).add(tId);
                            }

                            SolrInputDocument sd = new SolrInputDocument();
                            sd.addField("id",id);

                            for(Map.Entry<Integer,List<String>> hashLevel : topicsMap.entrySet()){

                                String fieldName = "topics"+hashLevel.getKey()+"_t";
                                String td = hashLevel.getValue().stream().map(i -> "t" + i).collect(Collectors.joining(" "));
                                Map<String,Object> updatedField = new HashMap<>();
                                updatedField.put("set", td);
                                sd.addField(fieldName, updatedField);
                            }

                            solrClient.add(sd);

                            LOG.info("Document " + id + " annotated [" + index.incrementAndGet() + "]");

                            if (index.get() % 100 == 0){
                                LOG.info("Committing partial annotations");
                                solrClient.commit();
                            }
                        }catch (Exception e){
                            LOG.error("Error annotating doc: " + solrDoc, e);
                        }
                    });
                }
                solrClient.commit();

                if (cursorMark.equals(nextCursorMark)) break;

                if (results.size() < window) break;

            }
            executor.awaitTermination(1, TimeUnit.HOURS);
            solrClient.commit();

            mailService.notifyAnnotation(annotationRequest,"Annotation completed");
            LOG.info("Completed!");
        }catch (Exception e){
            LOG.error("Unexpected error",e);
            mailService.notifyAnnotationError(annotationRequest, e.getMessage());
        }




    }

}
