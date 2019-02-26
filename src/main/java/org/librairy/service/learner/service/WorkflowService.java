package org.librairy.service.learner.service;

import org.librairy.service.learner.builders.CorpusBuilder;
import org.librairy.service.learner.facade.model.TopicsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

@Component
public class WorkflowService {

    private static final Logger LOG = LoggerFactory.getLogger(WorkflowService.class);

    @Autowired
    QueueService queueService;

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

    private ExecutorService executors;

    @PostConstruct
    public void setup(){
        executors = Executors.newSingleThreadExecutor();
        executors.submit(new Runnable() {
            @Override
            public void run() {

                while (true){
                    TopicsRequest request = null;
                    try{
                        LOG.info("Waiting for workspace creations ...");
                        request = queueService.getTopicsRequest();

                        // create a workspace
                        CorpusBuilder corpusBuilder = workspaceService.create(request);

                        // harvest documents
                        boolean result = collectorService.collect(corpusBuilder, request);
                        if (!result) continue;

                        // train a model
                        result = modelService.train(corpusBuilder, request);
                        if (!result) continue;

                        // export Docker image
                        exportService.request(request);

                    }catch (Exception e){
                        LOG.error("Error creating topics",e);
                        if (request != null) mailService.notifyError(request, "Model not created. For details consult with your system administrator.  ");
                    } finally{
                        if ((request != null) && (!request.getFrom().getCache())){
                            workspaceService.delete(request);
                        }
                    }

                }

            }
        });
    }

}
