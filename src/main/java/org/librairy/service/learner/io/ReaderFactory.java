package org.librairy.service.learner.io;

import com.google.common.base.Strings;
import org.librairy.service.learner.facade.model.DataFields;
import org.librairy.service.learner.facade.model.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class ReaderFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ReaderFactory.class);


    public static Reader newFrom(DataSource dataSource) throws IOException {


        String format   = dataSource.getFormat().toLowerCase();
        String url      = dataSource.getUrl();

        if (format.contains("solr")) return new SolrReader();
        else{

            InputStreamReader inputReader;

            if (url.startsWith("http")){
                if (url.endsWith("gz") || format.contains("gz")) {
                    inputReader = new InputStreamReader(new GZIPInputStream(new URL(url).openStream()));
                }else{
                    inputReader = new InputStreamReader(new URL(url).openStream());
                }
            }else{
                if (url.endsWith("gz")|| format.contains("gz")) {
                    inputReader = new InputStreamReader(new GZIPInputStream(new FileInputStream(url)));
                }else{
                    inputReader = new InputStreamReader(new FileInputStream(url));
                }
            }

            DataFields dataFields = dataSource.getFields();

            Map<String,String> parsingMap = new HashMap<>();
            parsingMap.put("id", dataFields.getId());
            parsingMap.put("text", dataFields.getText().get(0));
            if ((dataFields.getLabels() != null) && !dataFields.getLabels().isEmpty()) parsingMap.put("labels", dataFields.getLabels().get(0));

            if (format.contains("csv")){
                String separator = Strings.isNullOrEmpty(dataSource.getFilter())? "," : dataSource.getFilter();
                String labelsSeparator = " ";
                Map<String,Integer> map = new HashMap<>();
                parsingMap.entrySet().stream().forEach(entry -> map.put(entry.getKey(), Integer.valueOf(entry.getValue())));
                return new CSVReader(inputReader,separator, labelsSeparator, map);
            }else if (format.contains("jsonl")){
                return new JsonlReader(inputReader,parsingMap);
            }else throw new RuntimeException("Format not supported: " + format);
        }

    }

}
