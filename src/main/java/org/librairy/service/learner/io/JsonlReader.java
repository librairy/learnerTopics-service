package org.librairy.service.learner.io;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.librairy.service.learner.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class  JsonlReader implements Reader{

    private static final Logger LOG = LoggerFactory.getLogger(JsonlReader.class);
    private final Map<String, String> map;
    private final String path;

    private BufferedReader reader;

    public JsonlReader(File jsonFile,Map<String,String> map) throws IOException {
        this.path = jsonFile.getAbsolutePath();
        this.reader = path.endsWith(".gz")?
                new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(jsonFile)))) :
                new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile))) ;
        this.map = map;
    }

    public JsonlReader(InputStreamReader input,Map<String,String> map) throws IOException {
        this.path   ="inputStream";
        this.reader = new BufferedReader(input);
        this.map    = map;
    }


    @Override
    public Optional<Document> next()  {
        String line;
        try{
            if ((line = reader.readLine()) == null){
                reader.close();
                return Optional.empty();
            }
            Document document = new Document();

            JSONObject jsonObject = new JSONObject(line);

            if (map.containsKey("id"))      document.setId(jsonObject.getString(map.get("id")).replaceAll("\\P{Print}", ""));
            if (map.containsKey("text") && jsonObject.has(map.get("text")))    {
                try{
                    document.setText(jsonObject.getString(map.get("text")).replaceAll("\\P{Print}", ""));
                }catch (JSONException e){
                    LOG.warn(""+e.getMessage());
                }
            }
            if (map.containsKey("labels")){

                List<String> labels = new ArrayList<>();
                Object labelObject = jsonObject.get(map.get("labels"));

                if (labelObject instanceof JSONArray){

                    JSONArray labelList = jsonObject.getJSONArray(map.get("labels"));

                    for(int i=0;i<labelList.length();i++){
                        String label = (String) labelList.get(i);
                        labels.add(label.replaceAll("\\P{Print}", ""));
                    }

                }else{
                    labels.add(jsonObject.getString(map.get("labels")));
                }

                document.setLabels(labels);
            }

            return Optional.of(document);

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
