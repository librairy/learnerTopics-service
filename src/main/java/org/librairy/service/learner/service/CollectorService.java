package org.librairy.service.learner.service;

import com.google.common.base.Strings;
import org.librairy.service.learner.builders.CorpusBuilder;
import org.librairy.service.learner.executors.ParallelExecutor;
import org.librairy.service.learner.facade.model.DataSource;
import org.librairy.service.learner.facade.model.TopicsRequest;
import org.librairy.service.learner.io.Reader;
import org.librairy.service.learner.io.ReaderFactory;
import org.librairy.service.learner.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

@Component
public class CollectorService {

    private static final Logger LOG = LoggerFactory.getLogger(CollectorService.class);

    @Autowired
    MailService mailService;

    public boolean collect(CorpusBuilder corpusBuilder, TopicsRequest request) {

        try{
            Boolean multigrams = request.getParameters() != null && request.getParameters().containsKey("multigrams")? Boolean.valueOf(request.getParameters().get("multigrams")) : false;

            DataSource datasource = request.getFrom();

            LOG.info("Building bag-of-words with "+ (multigrams? "multigrams" : "unigrams") + " from: "  + datasource);

            Reader reader = ReaderFactory.newFrom(datasource);

            Long maxSize = datasource.getSize();
            AtomicInteger counter = new AtomicInteger();
            Integer interval = maxSize > 0? maxSize > 100? maxSize.intValue()/100 : 1 : 100;
            Optional<Document> doc;
            reader.offset(datasource.getOffset().intValue());
            ParallelExecutor parallelExecutor = new ParallelExecutor();
            while(( maxSize<0 || counter.get()<maxSize) &&  (doc = reader.next()).isPresent()){
                final Document document = doc.get();
                if (Strings.isNullOrEmpty(document.getText())) continue;
                if (counter.incrementAndGet() % interval == 0) LOG.info(counter.get() + " documents indexed");
                parallelExecutor.submit(() -> {
                    try {
                        corpusBuilder.add(document, multigrams, false);
                    } catch (Exception e) {
                        LOG.error("Unexpected error adding new document to corpus",e);
                    }
                });
            }
            parallelExecutor.awaitTermination(1, TimeUnit.HOURS);
            mailService.notifyCreation(request, "Datasource analyzed. Ready to create a new topic model.");
            return true;
        }catch (Exception e){
            LOG.error("Unexpected error harvesting datasource: " + request, e);
            mailService.notifyError(request, "Datasource error. For details consult with your system administrator.");
            return false;
        }finally{
            corpusBuilder.close();
        }
    }
}
