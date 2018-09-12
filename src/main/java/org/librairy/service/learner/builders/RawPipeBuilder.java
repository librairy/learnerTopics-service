package org.librairy.service.learner.builders;

import cc.mallet.pipe.*;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import com.google.common.base.Strings;
import org.librairy.service.learner.executors.ParallelExecutor;
import org.librairy.service.nlp.facade.model.PoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class RawPipeBuilder implements PipeBuilderI{

    private static final Logger LOG = LoggerFactory.getLogger(RawPipeBuilder.class);

    private final Integer size;

    public RawPipeBuilder(Integer size) {
        this.size = size;
    }

    public Pipe build(String pos, Boolean enableTarget, TokenSequenceRemoveStopwords stopWordTokenizer) {
        ArrayList pipeList = new ArrayList();

        // Read data from File objects
        pipeList.add(new Input2CharSequence("UTF-8"));

        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\S+")));

        pipeList.add(stopWordTokenizer);

        pipeList.add(new TokenSequence2FeatureSequence());

        // Do the same thing for the "target" field:
        //  convert a class label string to a Label object,
        //  which has an index in a Label alphabet.
//        pipeList.add(new Target2Label());

        if (enableTarget) pipeList.add(new TargetStringToFeatures());


        // Now convert the sequence of features to a sparse vector,
        //  mapping feature IDs to counts.
//        pipeList.add(new FeatureSequence2FeatureVector());


        // Print out the features and the label
//        pipeList.add(new PrintInputAndTarget());

        return new SerialPipes(pipeList);
    }

    /**
     * @param cvsIterator
     * @param stopWordTokenizer the tokenizer that will be used to write instances
     * @param pos
     * @param minFreq Reduce words to those that occur more than N times.
     * @param docProportionCutoff Remove features that occur in more than (X*100)% of documents. 0.05 is equivalent to IDF of 3.0.
     * @return
     */
    public void prune(Iterator<Instance> cvsIterator, TokenSequenceRemoveStopwords stopWordTokenizer, String pos, Integer minFreq, Double docProportionCutoff){

        ArrayList pipeList = new ArrayList();
        Alphabet alphabet = new Alphabet();

        // Read data from File objects
        pipeList.add(new Input2CharSequence("UTF-8"));

        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\S+")));

        pipeList.add(stopWordTokenizer);

        pipeList.add(new TokenSequence2FeatureSequence(alphabet));

        FeatureCountPipe featureCounter = new FeatureCountPipe(alphabet, null);
        if (minFreq > 0) pipeList.add(featureCounter);

        FeatureDocFreqPipe docCounter = new FeatureDocFreqPipe(alphabet, null);
        if (docProportionCutoff < 1.0) pipeList.add(docCounter);

        Pipe pipe = new SerialPipes(pipeList);

        Instant startProcess = Instant.now();

        /**
         *
         */
        Iterator<Instance> iterator = pipe.newParallelIteratorFrom(cvsIterator);

        ParallelExecutor executors = new ParallelExecutor();

        LOG.info("Getting stats to complete prune actions ..");
        int count = 0;
        int interval = size < 100? 10 : size/100;
        while(iterator.hasNext()){

            try{
                count++;
                if (count % interval == 0) {
                    LOG.info("Docs analyzed: " + count);
                    Thread.sleep(10);
                }
                executors.submit(() -> {
                    try {
                        iterator.next();
                    }catch (Exception e){
                        LOG.error("Instance not handled by pipe: " + e.getMessage());
                    }
                });
            }catch (Exception e){
                LOG.error("Error reading next instance",e);
                break;
            }

        }
        LOG.info("Waiting for finish stats ...");
        executors.awaitTermination(1, TimeUnit.MINUTES);
        Instant endProcess = Instant.now();

        String durationProcess = ChronoUnit.HOURS.between(startProcess, endProcess) + "hours "
                + ChronoUnit.MINUTES.between(startProcess, endProcess) % 60 + "min "
                + (ChronoUnit.SECONDS.between(startProcess, endProcess) % 60) + "secs "
                + (ChronoUnit.MILLIS.between(startProcess, endProcess) % 60) + "msecs";


        LOG.info("Prune stats collected in: " + durationProcess);

        if (minFreq > 0) {
            List<String> stopWordsByFreq = featureCounter.getPrunedWords(minFreq);
            LOG.info(stopWordsByFreq.size() + " words pruned by freq [" + minFreq + "]");
            stopWordTokenizer.addStopWords(stopWordsByFreq);
        }
        if (docProportionCutoff > 0.0) {
            List<String> stopWordsByDocFreq = docCounter.getPrunedWords(docProportionCutoff);
            LOG.info(stopWordsByDocFreq.size() + " words pruned by doc-freq [" + docProportionCutoff + "]");
            stopWordTokenizer.addStopWords(stopWordsByDocFreq);
        }

    }

}
