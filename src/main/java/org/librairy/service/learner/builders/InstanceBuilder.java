package org.librairy.service.learner.builders;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import org.librairy.service.learner.executors.ParallelExecutor;
import org.librairy.service.learner.model.Reader;
import org.librairy.service.learner.service.CorpusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class InstanceBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(InstanceBuilder.class);


    @Value("#{environment['resource.folder']?:'${resource.folder}'}")
    String resourceFolder;

    @Autowired
    CorpusService corpusService;

    /**
     *
     * @param filePath
     * @param regEx
     * @param textIndex
     * @param labelIndex
     * @param idIndex
     * @param enableTarget
     * @param pos
     * @param minFreq Reduce words to those that occur more than N times.
     * @param maxDocRatio Remove words that occur in more than (X*100)% of documents. 0.05 is equivalent to IDF of 3.0.
     * @return
     */
    public InstanceList getInstances(String filePath, String regEx, int textIndex, int labelIndex, int idIndex, boolean enableTarget, String pos, Integer minFreq, Double maxDocRatio, Boolean raw) throws IOException {

        Integer size = corpusService.getNumDocs();

        PipeBuilderI pipeBuilder = PipeBuilderFactory.newInstance(size, raw);

        File stoplist = Paths.get(Paths.get(resourceFolder).getParent().toString(), "stopwords.txt").toFile();

        TokenSequenceRemoveStopwords tokenizer;

        if (!stoplist.exists()){
            LOG.info("No stopwords file found");
            tokenizer = new TokenSequenceRemoveStopwords(false, false);
        }else{
            LOG.info("Using stopwords file from: " + stoplist.getAbsolutePath());
            tokenizer = new TokenSequenceRemoveStopwords(stoplist, "UTF-8", true, false, false);
        }

        if (minFreq > 0 || maxDocRatio < 1.0){

            pipeBuilder.prune(new ReaderBuilder().fromCSV(filePath, regEx, textIndex, labelIndex, idIndex).getIterator(), tokenizer, pos, minFreq, maxDocRatio);

        }

        Reader csvReader = new ReaderBuilder().fromCSV(filePath, regEx, textIndex, labelIndex, idIndex);

        CsvIterator cvsIterator = csvReader.getIterator();

        Pipe pipe = pipeBuilder.build(pos, enableTarget, tokenizer);

        Instant startProcess = Instant.now();

        InstanceList instances = new InstanceList(pipe);

        ParallelExecutor executors = new ParallelExecutor();

        LOG.info("processing documents in a parallel BoW-pipe builder ..");
        AtomicInteger counter = new AtomicInteger();
        int interval = size < 100? 10 : size/100;
        while(cvsIterator.hasNext()){

            try{
                final Instance rawInstance = cvsIterator.next();
                if (counter.incrementAndGet() % interval == 0) {
                    LOG.info(counter.get() + " docs processed");
//                    Thread.sleep(10);
                }
                executors.submit(() -> {
                    try {
                        instances.addThruPipe(rawInstance);
                    }catch (Exception e){
                        LOG.error("Instance not handled by pipe: " + e.getMessage());
                        instances.remove(rawInstance);
                    }
                });
            }catch (IllegalStateException e){
                LOG.warn("Error reading next instance",e);
            }catch (Exception e){
                LOG.error("Error reading next instance",e);
                break;
            }

        }

        LOG.info("Waiting for finish instances ...");
        executors.awaitTermination(1, TimeUnit.MINUTES);


        Instant endProcess = Instant.now();

        String durationProcess = ChronoUnit.HOURS.between(startProcess, endProcess) + "hours "
                + ChronoUnit.MINUTES.between(startProcess, endProcess) % 60 + "min "
                + (ChronoUnit.SECONDS.between(startProcess, endProcess) % 60) + "secs "
                + (ChronoUnit.MILLIS.between(startProcess, endProcess) % 60) + "msecs";


        LOG.info("Docs processed in: " + durationProcess);

        csvReader.close();

        return instances;

    }



}
