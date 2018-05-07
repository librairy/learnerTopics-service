package cc.mallet.topics;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import com.google.common.base.CharMatcher;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import org.librairy.service.learner.builders.PipeBuilder;
import org.librairy.service.learner.executors.ParallelExecutor;
import org.librairy.service.modeler.clients.LibrairyNlpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class CSVReader {

    private static final Logger LOG = LoggerFactory.getLogger(CSVReader.class);

    @Value("#{environment['NLP_ENDPOINT']?:'${nlp.endpoint}'}")
    String nlpEndpoint;

    @Autowired
    LibrairyNlpClient client;

    @PostConstruct
    public void setup(){
        LOG.info("component initialized");
    }

    public void setNlpEndpoint(String nlpEndpoint) {
        this.nlpEndpoint = nlpEndpoint;
    }



    public InstanceList getSerialInstances(String filePath, String language, String regEx, int textIndex, int labelIndex, int idIndex, boolean enableTarget, String pos) throws IOException {

        Pipe pipe = new PipeBuilder().build(client, language, pos, enableTarget);
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

    public InstanceList getParallelInstances(String filePath, String language, String regEx, int textIndex, int labelIndex, int idIndex, boolean enableTarget, String pos) throws IOException {


        Pipe pipe = new PipeBuilder().build(client, language, pos, enableTarget);
        InstanceList instances = new InstanceList(pipe);

        int dataGroup           = textIndex;
        int targetGroup         = labelIndex;
        int uriGroup            = idIndex;

        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filePath)), "UTF-8"));

        CsvIterator iterator = new CsvIterator(reader, regEx, dataGroup, targetGroup, uriGroup);

        ParallelExecutor executors = new ParallelExecutor();

        LOG.info("processing documents in a parallel pipe builder ..");
        AtomicInteger counter = new AtomicInteger();
        while(iterator.hasNext()){

            try{
                final Instance rawInstance = iterator.next();
                if (counter.incrementAndGet() % 100 == 0) {
                    LOG.info(counter.get() + " docs processed");
                    Thread.sleep(20);
                }
                executors.submit(() -> {
                    try {

                        Pattern labelPattern = Pattern.compile("[A-Za-z0-9-.@_~#áéíóúÁÉÍÓÚñÑ]+");
                        Object target = rawInstance.getTarget();
                        Boolean invalid = false;
                        if (target != null) {
                            String[] labels = ((String) target).split(" ");
                            invalid = Arrays.stream(labels).filter(label -> !labelPattern.matcher(label).matches()).count() > 0;
                            if (invalid){
                                System.out.println("hi");
                            }
                        }

                        instances.addThruPipe(rawInstance);
                    }catch (NumberFormatException e){
                        LOG.warn("Instance not handled by pipe: " + e.getMessage());
                        instances.remove(rawInstance);
                    }catch (Exception e){
                        LOG.error("Instance not handled by pipe",e);
                        instances.remove(rawInstance);
                    }
                });
            }catch (Exception e){
                LOG.error("Error reading next instance",e);
                break;
            }

        }

        LOG.info("Waiting for finish instances ...");
        executors.awaitTermination(1, TimeUnit.MINUTES);
        LOG.info("Completed!");

        reader.close();

        return instances;
    }
}
