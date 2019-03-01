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

public class TopicExplorerIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(TopicExplorerIntTest.class);

    private static final Integer window = 500;

    String collectionEndpoint = "http://librairy.linkeddata.es/solr/documents";

    String filter = "source_s:oo-api";

    String refId = "ocds-b5fd17-0afce6a1-9c93-4d8b-b11a-bfb440d70601-270815-wsh486499";

    private HttpSolrClient solrClient;

    @Before
    public void setup(){
        solrClient = new HttpSolrClient.Builder(collectionEndpoint).build();
    }



    @Test
    public void execute(){


        try{

            SolrQuery refQuery = new SolrQuery();
            refQuery.setRows(1);
            Arrays.asList("name_s","id","topics0_t","topics1_t","topics2_t").forEach(f -> refQuery.addField(f));
            refQuery.setQuery("id:"+refId);
            QueryResponse rsp = solrClient.query(refQuery);
            if (rsp.getResults().isEmpty()){
                LOG.warn("Document " + refId + " not found!");
                return;
            }

            SolrDocument refDoc = rsp.getResults().get(0);
            LOG.info("RefDoc: " + refDoc);

            SolrQuery simQuery = new SolrQuery();
            simQuery.addFilterQuery(filter);

            List<String> booleanClauses = new ArrayList<>();


            Arrays.asList(0,1,2).stream().map(i -> composeQuery(refDoc, "topics"+i+"_t", "OR")).forEach(clause -> booleanClauses.add(clause));

//            if (refDoc.containsKey("topics0_t")){
//                String ps = Arrays.stream(((String) refDoc.getFieldValue("topics0_t")).split(" ")).map(t -> "topics0_t:" + t).collect(Collectors.joining(" OR "));
//                booleanClauses.add(ps);
//            }

            String finalQuery = booleanClauses.stream().collect(Collectors.joining(" OR "));

            simQuery.setQuery(finalQuery);
            simQuery.setRows(10);
            simQuery.setFields("id","name_s","score");
            QueryResponse simDocs = solrClient.query(simQuery);

            if (simDocs.getResults().isEmpty()){
                LOG.warn("No similar docs found!");
                return;
            }

            SolrDocumentList results = simDocs.getResults();
            for (SolrDocument result : results){
                if ( ((String)result.getFieldValue("id")).equalsIgnoreCase(refId)) continue;
                LOG.info("Result: " + result);
            }



            LOG.info("Completed!");
        }catch (Exception e){
            LOG.error("Unexpected error",e);
        }
    }

    private String composeQuery(SolrDocument refDoc, String field, String op){
        return Arrays.stream(((String) refDoc.getFieldValue(field)).split(" ")).map(t -> field+":" + t).collect(Collectors.joining(" "+op.toUpperCase()+" "));
    }

}
