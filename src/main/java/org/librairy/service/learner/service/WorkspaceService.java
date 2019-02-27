package org.librairy.service.learner.service;

import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.librairy.service.learner.builders.CorpusBuilder;
import org.librairy.service.learner.facade.model.DataSource;
import org.librairy.service.learner.facade.model.TopicsRequest;
import org.librairy.service.modeler.clients.LibrairyNlpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class WorkspaceService {

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceService.class);

    @Autowired
    LibrairyNlpClient librairyNlpClient;

    @Value("#{environment['OUTPUT_DIR']?:'${output.dir}'}")
    String outputDir;


    public CorpusBuilder create(TopicsRequest request) throws IOException {
        Path workspacePath = getPath(request);
        if (!workspacePath.toFile().getParentFile().exists()) workspacePath.toFile().getParentFile().mkdirs();
        CorpusBuilder corpusBuilder = new CorpusBuilder(workspacePath, librairyNlpClient);
        if (request.getFrom().getCache()) corpusBuilder.load();
        return corpusBuilder;
    }

    public void delete(TopicsRequest request){
        try {
            LOG.info("Deleting workspace (non-cached) from: " + request);
            Files.deleteIfExists(getPath(request));
        } catch (IOException e) {
            LOG.warn("Error deleting workspace from: " + request, e);
        }
    }

    public boolean clean(){

        try {
            Path outputPath = Paths.get(outputDir);
            FileUtils.cleanDirectory(outputPath.toFile());
            return true;
        } catch (Exception e) {
            LOG.warn("Error deleting workspaces", e);
            return false;
        }

    }

    private Path getPath(TopicsRequest request){

        // name
        StringBuilder workspaceName = new StringBuilder();
        workspaceName.append(request.getName().replaceAll("\\W+","-"));

        // filter
        DataSource datasource = request.getFrom();
        if (!Strings.isNullOrEmpty(datasource.getFilter())){
            workspaceName.append("-").append(datasource.getFilter().hashCode());
        }

        // size
        String size = datasource.getSize()<0? "full" : String.valueOf(datasource.getSize());
        workspaceName.append("-").append(size);

        // extension
        workspaceName.append(".csv.gz");

        return Paths.get(outputDir, workspaceName.toString());
    }

}
