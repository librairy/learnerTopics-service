package org.librairy.service.learner.io;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;
import org.librairy.service.learner.facade.model.DataFields;
import org.librairy.service.learner.facade.model.DataSource;
import org.librairy.service.learner.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SolrReader implements Reader {

    private static final Logger LOG = LoggerFactory.getLogger(SolrReader.class);

    private static final Integer window = 500;

    private final SolrClient solrClient;
    private final String endpoint;
    private final String collection;
    private final String filter;
    private final String idField;
    private final List<String> txtFields;
    private final List<String> labelsFields;

    private String nextCursorMark;
    private SolrQuery solrQuery;
    private String cursorMark;
    private SolrDocumentList solrDocList;
    private AtomicInteger index;


    public SolrReader(DataSource dataSource) throws IOException {


        DataFields fields = dataSource.getFields();
        this.idField        = fields.getId();
        this.txtFields      = fields.getText();
        this.labelsFields   = (fields.getLabels() != null)? fields.getLabels() : Collections.emptyList();

        this.filter         = Strings.isNullOrEmpty(dataSource.getFilter())? "*:*" : dataSource.getFilter();
        this.endpoint       = StringUtils.substringBeforeLast(dataSource.getUrl(),"/");
        this.collection     = StringUtils.substringAfterLast(dataSource.getUrl(),"/");

        this.solrClient     = new HttpSolrClient.Builder(endpoint).build();


        this.solrQuery = new SolrQuery();
        solrQuery.setRows(window);
        solrQuery.addField(idField);
        txtFields.forEach(f -> solrQuery.addField(f));
        labelsFields.forEach(f -> solrQuery.addField(f));
        solrQuery.setQuery(filter);
        solrQuery.addSort(idField, SolrQuery.ORDER.asc);
        this.nextCursorMark = CursorMarkParams.CURSOR_MARK_START;
        query();

    }

    private void query() throws IOException {
        try{
            this.cursorMark = nextCursorMark;
            solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
            QueryResponse rsp = solrClient.query(collection, solrQuery);
            this.nextCursorMark = rsp.getNextCursorMark();
            this.solrDocList = rsp.getResults();
            this.index = new AtomicInteger();
        }catch (Exception e){
            throw new IOException(e);
        }
    }

    @Override
    public Optional<Document> next() {
        try{
            if (index.get() >= solrDocList.size()) {
                if (index.get() < window){
                    return Optional.empty();
                }
                query();
            }

            if (cursorMark.equals(nextCursorMark)) {
                return Optional.empty();
            }

            SolrDocument solrDoc = solrDocList.get(index.getAndIncrement());

            Document document = new Document();

            String id = (String) solrDoc.get(idField);
            document.setId(id);

            StringBuilder txt = new StringBuilder();
            txtFields.stream().filter(tf -> solrDoc.containsKey(tf)).forEach(tf -> txt.append(StringReader.hardFormat(solrDoc.getFieldValue(tf).toString())).append(" "));
            document.setText(txt.toString());

            if (!labelsFields.isEmpty()){
                StringBuilder labels = new StringBuilder();
                labelsFields.stream().filter(tf -> solrDoc.containsKey(tf)).forEach(tf -> labels.append(StringReader.softFormat(solrDoc.getFieldValue(tf).toString())).append(" "));
                document.setLabels(Arrays.asList(labels.toString().split(" ")));
            }

            return Optional.of(document);
        }catch (Exception e){
            LOG.error("Unexpected error on iterated list of solr docs",e);
            if (e instanceof java.lang.IndexOutOfBoundsException) return Optional.empty();
            return Optional.of(new Document());
        }

    }

    @Override
    public void offset(Integer numLines) {
        this.index = new AtomicInteger(numLines);
    }
}
