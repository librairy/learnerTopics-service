package org.librairy.service.learner.service;

import org.apache.commons.io.FileUtils;
import org.librairy.service.learner.builders.CorpusBuilder;
import org.librairy.service.learner.facade.model.TopicsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.nio.file.*;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

@Component
public class TopicService {

    private static final Logger LOG = LoggerFactory.getLogger(TopicService.class);

    @Value("#{environment['OUTPUT_DIR']?:'${output.dir}'}")
    String outputDir;

    @Value("#{environment['RESOURCE_FOLDER']?:'${resource.folder}'}")
    String resourceDir;

    @Autowired
    MailService mailService;

    @Autowired
    WorkspaceService workspaceService;

    @Autowired
    ModelService modelService;

    @Autowired
    ExportService exportService;

    @Autowired
    CollectorService collectorService;


    public void create(TopicsRequest request){
        try{
            LOG.info("Ready to create topics from: " + request);
            // create a workspace
            CorpusBuilder corpusBuilder = workspaceService.create(request);

            // harvest documents
            if (corpusBuilder.getNumDocs() <= 0){
                boolean result = collectorService.collect(corpusBuilder, request);
                if (!result) return;
            }

            // train a model
            boolean result = modelService.train(corpusBuilder, request);
            if (!result) return;

            // export Docker image
            exportService.request(request);

            // doctopics annotation
            if (request.getAnnotate()){
                Path source = Paths.get(resourceDir, "doctopics.csv.gz");
                String name = corpusBuilder.getFilePath().toFile().getName();
                String workspace = StringUtils.substringBefore(name, ".");
                Path target = Paths.get(outputDir, workspace+"-doctopics.csv.gz");
                Files.move(source, target, new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
                LOG.info("Moved doctopics to: " + target);
            }

            FileUtils.cleanDirectory(Paths.get(resourceDir).toFile());

        }catch (Exception e){
            LOG.error("Error creating topics",e);
            if (request != null) mailService.notifyModelError(request, "Model not created. For details consult with your system administrator.  ");
        } finally{
            if ((request != null) && (request.get("from")!= null) && (request.getFrom().get("cache") != null) && (!request.getFrom().getCache())){
                workspaceService.delete(request);
            }
        }
    }

}
