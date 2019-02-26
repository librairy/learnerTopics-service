package org.librairy.service.learner.io;

import org.librairy.service.learner.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class CSVReader implements Reader{

    private static final Logger LOG = LoggerFactory.getLogger(CSVReader.class);
    private final String separator;
    private final String path;
    private final Map<String, Integer> map;
    private final String labelSeparator;

    private BufferedReader reader;

    public CSVReader(File csvFile, String separator, String labelSeparator, Map<String,Integer> map) throws IOException {
        this.path = csvFile.getAbsolutePath();
        this.reader = path.endsWith(".gz")?
                new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(csvFile)))) :
                new BufferedReader(new InputStreamReader(new FileInputStream(csvFile))) ;
        this.separator = separator;
        this.labelSeparator = labelSeparator;
        this.map = map;
    }

    public CSVReader(InputStreamReader input,String separator, String labelSeparator, Map<String,Integer> map) throws IOException {
        this.path   ="inputStream";
        this.reader = new BufferedReader(input);
        this.map    = map;
        this.separator = separator;
        this.labelSeparator = labelSeparator;
    }

    @Override
    public Optional<Document> next() {
        String line = null;
        try {
            if ((line = reader.readLine()) == null) {
                reader.close();
                return Optional.empty();
            }

            String[] values = line.split(separator);

            Document document = new Document();

            if (map.containsKey("id")) document.setId(values[map.get("id")].replaceAll("\\P{Print}", ""));
            if (map.containsKey("text")) document.setText(values[map.get("text")].replaceAll("\\P{Print}", ""));
            if (map.containsKey("labels"))
                document.setLabels(Arrays.asList(values[map.get("labels")].split(labelSeparator)).stream().map(l -> l.replaceAll("\\P{Print}", "")).collect(Collectors.toList()));

            return Optional.of(document);
        } catch (ArrayIndexOutOfBoundsException e){
            LOG.warn("Invalid row("+e.getMessage()+") - [" + line + "]");
            return Optional.of(new Document());
        }catch (Exception e){
            LOG.error("Unexpected error parsing file: " + path,e);
            return Optional.of(new Document());
        }
    }

    @Override
    public void offset(Integer numLines) {
        if (numLines>0){
            AtomicInteger counter = new AtomicInteger();
            String line;
            try{
                while (((line = reader.readLine()) != null) && (counter.incrementAndGet() <= numLines)){
                }

            }catch (Exception e){
                LOG.error("Unexpected error parsing file: " + path,e);
            }


        }
    }

}
