package org.librairy.service.learner.builders;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SimpleTokenizer;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import org.librairy.service.learner.executors.ParallelExecutor;
import org.librairy.service.learner.model.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class InstanceBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(InstanceBuilder.class);


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
    public InstanceList getInstances(String filePath, String regEx, int textIndex, int labelIndex, int idIndex, boolean enableTarget, String pos, Integer minFreq, Double maxDocRatio) throws IOException {


        PipeBuilder pipeBuilder = new PipeBuilder();

        TokenSequenceRemoveStopwords tokenizer = new TokenSequenceRemoveStopwords(false, false);

        if (minFreq > 0 || maxDocRatio < 1.0){

            pipeBuilder.prune(new ReaderBuilder().fromCSV(filePath, regEx, textIndex, labelIndex, idIndex).getIterator(), tokenizer, pos, minFreq, maxDocRatio);

        }

        Reader csvReader = new ReaderBuilder().fromCSV(filePath, regEx, textIndex, labelIndex, idIndex);

        CsvIterator cvsIterator = csvReader.getIterator();

        Pipe pipe = pipeBuilder.build(pos, enableTarget, tokenizer);

        InstanceList instances = new InstanceList(pipe);

        ParallelExecutor executors = new ParallelExecutor();

        LOG.info("processing documents in a parallel BoW-pipe builder ..");
        AtomicInteger counter = new AtomicInteger();
        while(cvsIterator.hasNext()){

            try{
                final Instance rawInstance = cvsIterator.next();
                if (counter.incrementAndGet() % 100 == 0) {
                    LOG.info(counter.get() + " docs processed");
                    Thread.sleep(20);
                }
                executors.submit(() -> {
                    try {
                        instances.addThruPipe(rawInstance);
                    }catch (Exception e){
                        LOG.error("Instance not handled by pipe: " + e.getMessage());
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

        csvReader.close();

        return instances;

    }



}
