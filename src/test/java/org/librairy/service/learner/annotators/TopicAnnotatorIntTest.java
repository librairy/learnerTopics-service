package org.librairy.service.learner.annotators;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.assertj.core.util.Strings;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.librairy.service.learner.executors.ParallelExecutor;
import org.librairy.service.modeler.facade.rest.model.ClassRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicAnnotatorIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(TopicAnnotatorIntTest.class);

    private static final Integer window = 500;

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

    }

    String modelEndpoint = "http://librairy.linkeddata.es/jrc-en-model";

    String collectionEndpoint = "http://librairy.linkeddata.es/solr/documents";

    List<String> filters = Arrays.asList("source_s:jrc","lang_s:en");

    private HttpSolrClient solrClient;

    @Before
    public void setup(){
        solrClient = new HttpSolrClient.Builder(collectionEndpoint).build();
    }



    @Test
    public void execute(){


        try{

            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setRows(window);

            Arrays.asList("id","txt_t").forEach(f -> solrQuery.addField(f));

            solrQuery.setQuery("*:*");
            filters.forEach(f -> solrQuery.addFilterQuery(f));
            solrQuery.addSort("id", SolrQuery.ORDER.asc);

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
                                    .post(modelEndpoint + "/classes")
                                    .header("accept", "application/json")
                                    .header("Content-Type", "application/json")
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

            LOG.info("Completed!");
        }catch (Exception e){
            LOG.error("Unexpected error",e);
        }


    }


    @Test
    public void addDocument(){

        try{
            SolrInputDocument sd = new SolrInputDocument();
            sd.addField("id","ocds-b5fd17-0053683d-c603-4383-95b5-27fe4c753449-270819-cambdc001-dn351454-86049260");
            sd.addField("name_s","Cambridge City Council - Air Quality Modelling for Greater Cambridge Partnership");
            sd.addField("txt_t","This project is in two parts: Part 1 is to prepare a baseline model to enable assessment of air quality effects associated with Greater Cambridge Partnership (GCP) Projects, within the GCP area. Requirements: - Compilation of a Detailed Dispersion Model of the GCP area including all sources (road, point and area) for a baseline year (2017) and a future year (2031) without GCP interventions. - Model to be verified against existing monitoring data available. - Use of latest highways automatic traffic count data and recent ANPR study data to create an emissions inventory and source apportionment study, to be utilised within the Detailed Dispersion Model. Part 2 is to model interventions as proposed by the GCP using the baseline model to assess their effect on air quality within the GCP area. As yet these interventions have not been defined. The contract requires the tenderer to submit rates which can be called upon in order to undertake the further work. The further work will be scoped and a brief sent to the tenderer to provide a methodology and price (using the agreed rates) in order to undertake the work. The contract will be required until 2021 initially with the potential for further interventions to be modelled after this date with an extension to the contract. This may require an update to the baseline model stated in Part 1.");
            sd.addField("size_i",1351);
            sd.addField("labels_t","sewage refuse cleaning_and_environmental_services");
            sd.addField("format_s","json");
            sd.addField("lang_s","en");
            sd.addField("source_s","oo-api");
            sd.addField("topics0_t","td0");
            sd.addField("topics1_t","td1");
            sd.addField("topics2_t","td2");

            solrClient.add(sd);
            solrClient.commit();

        }catch (Exception e){
            LOG.error("Unexpected error",e);
        }


    }

}
