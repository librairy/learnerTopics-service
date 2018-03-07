package org.librairy.service.learner.service;

import com.google.common.base.Strings;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMessages_zh_CN;
import org.librairy.service.learner.facade.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class CorpusService {

    private static final Logger LOG = LoggerFactory.getLogger(CorpusService.class);

    @Value("#{environment['OUTPUT_DIR']?:'${output.dir}'}")
    String outputDir;

    public static final String SEPARATOR = ";;";

    private BufferedWriter writer;
    private Path filePath;
    private Boolean isClosed = false;
    private AtomicInteger counter   = new AtomicInteger(0);
    private String updated = "";

    @PostConstruct
    public void setup() throws IOException {
        initialize();
    }

    @PreDestroy
    public void destroy() throws IOException {
        close();
    }

    public String getUpdated() {
        return updated;
    }

    public Integer getNumDocs(){
        return counter.get();
    }

    public synchronized void add(Document document) throws IOException {
        StringBuilder row = new StringBuilder();
        row.append(document.getId()).append(SEPARATOR);
        row.append(document.getName()).append(SEPARATOR);
        String labels = document.getLabels().stream().collect(Collectors.joining(" "));
        if (Strings.isNullOrEmpty(labels)) labels = "default";
        row.append(labels).append(SEPARATOR);
        row.append(document.getText());
        updated = TimeService.now();
        if (isClosed) initialize();
        writer.write(row.toString()+"\n");
        counter.incrementAndGet();
        LOG.info("Added document: [" + document.getId() + " | " + document.getName() + "] to corpus");
    }

    public void remove() throws IOException {
        LOG.info("Corpus deleted");
        counter.set(0);
        close();
        filePath.toFile().delete();
        initialize();
    }

    private synchronized void initialize() throws IOException {
        LOG.info("Corpus initialized");
        filePath = getFilePath();

        if (filePath.toFile().exists()){
            try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filePath.toFile()))));
                counter.set(Long.valueOf(reader.lines().count()).intValue());
                updated = TimeService.from(filePath.toFile().lastModified());
                reader.close();
            }catch (Exception e){
                LOG.debug("Error reading lines in existing file: " + filePath,e);
            }
        }else{
            filePath.toFile().getParentFile().mkdirs();
        }

        writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(filePath.toFile(),true))));
        setClosed(false);
    }

    public void close() throws IOException {
        setClosed(true);
        if (writer != null){
            try{
                writer.flush();
                writer.close();
            }catch (IOException e){
                LOG.debug("Writer closing error",e);
            }
        }
        LOG.info("Corpus closed");
    }

    private synchronized void setClosed(Boolean status){
        this.isClosed = status;
    }

    public Path getFilePath(){
        return  Paths.get(outputDir, "corpus.csv.gz");
    }

}
