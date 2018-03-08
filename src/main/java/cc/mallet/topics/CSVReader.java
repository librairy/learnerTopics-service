package cc.mallet.topics;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import org.librairy.service.learner.builders.PipeBuilder;
import org.librairy.service.learner.executors.ParallelExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class CSVReader {

    private static final Logger LOG = LoggerFactory.getLogger(CSVReader.class);

    @Value("#{environment['NLP_ENDPOINT']?:'${nlp.endpoint}'}")
    String nlpEndpoint;


    @PostConstruct
    public void setup(){

    }

    public void setNlpEndpoint(String nlpEndpoint) {
        this.nlpEndpoint = nlpEndpoint;
    }


    public InstanceList getSerialInstances(String filePath, String language, String regEx, int textIndex, int labelIndex, int idIndex) throws IOException {

        // Construct a new instance list, passing it the pipe we want to use to process instances.

        String nlpServiceEndpoint = nlpEndpoint.replace("%%", language);

        Pipe pipe = new PipeBuilder().build(nlpServiceEndpoint);
        InstanceList instances = new InstanceList(pipe);

        int dataGroup           = textIndex;
        int targetGroup         = labelIndex;
        int uriGroup            = idIndex;


        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filePath))));

        CsvIterator iterator = new CsvIterator(reader, regEx, dataGroup, targetGroup, uriGroup);

        // Now process each instance provided by the iterator.
        instances.addThruPipe(iterator);

        reader.close();

        return instances;
    }

    public InstanceList getParallelInstances(String filePath, String language, String regEx, int textIndex, int labelIndex, int idIndex) throws IOException {

        // Construct a new instance list, passing it the pipe we want to use to process instances.
        String nlpServiceEndpoint = nlpEndpoint.replace("%%", language);

        Pipe pipe = new PipeBuilder().build(nlpServiceEndpoint);
        InstanceList instances = new InstanceList(pipe);

        int dataGroup           = textIndex;
        int targetGroup         = labelIndex;
        int uriGroup            = idIndex;

        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filePath))));

        CsvIterator iterator = new CsvIterator(reader, regEx, dataGroup, targetGroup, uriGroup);

        ParallelExecutor executors = new ParallelExecutor();

        AtomicInteger counter = new AtomicInteger();
        while(iterator.hasNext()){

            int index = counter.incrementAndGet();
            final Instance rawInstance = iterator.next();
            executors.submit(() -> {
                LOG.info("processing document: " + index);
                instances.addThruPipe(rawInstance);
            });

        }

        LOG.info("Waiting for "+counter.get() + " instances ...");
        executors.awaitTermination(counter.get(), TimeUnit.MINUTES);
        LOG.info("Completed!");

        reader.close();

        return instances;
    }
}
