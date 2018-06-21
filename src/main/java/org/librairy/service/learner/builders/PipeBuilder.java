package org.librairy.service.learner.builders;

import cc.mallet.pipe.*;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import com.google.common.base.Strings;
import org.librairy.service.nlp.facade.model.PoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class PipeBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(PipeBuilder.class);

    public PipeBuilder() {
    }

    public Pipe build(String pos, Boolean enableTarget, TokenSequenceRemoveStopwords stopWordTokenizer) {
        ArrayList pipeList = new ArrayList();

        // Read data from File objects
        pipeList.add(new Input2CharSequence("UTF-8"));

        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\S+")));

        List<PoS> posList = Strings.isNullOrEmpty(pos) ? Collections.emptyList() : Arrays.asList(pos.split(" ")).stream().map(i -> PoS.valueOf(i.toUpperCase())).collect(Collectors.toList());
        pipeList.add(new TokenSequenceRemovePoS(posList));

        pipeList.add(new TokenSequenceExpandBoW("="));

        pipeList.add(new TokenSequenceRemoveNonAlpha());

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

        List<PoS> posList = Strings.isNullOrEmpty(pos) ? Collections.emptyList() : Arrays.asList(pos.split(" ")).stream().map(i -> PoS.valueOf(i.toUpperCase())).collect(Collectors.toList());
        pipeList.add(new TokenSequenceRemovePoS(posList));

        pipeList.add(new TokenSequenceExpandBoW("="));

        pipeList.add(new TokenSequenceRemoveNonAlpha());

        pipeList.add(stopWordTokenizer);

        pipeList.add(new TokenSequence2FeatureSequence(alphabet));

        FeatureCountPipe featureCounter = new FeatureCountPipe(alphabet, null);
        if (minFreq > 0) pipeList.add(featureCounter);

        FeatureDocFreqPipe docCounter = new FeatureDocFreqPipe(alphabet, null);
        if (docProportionCutoff < 1.0) pipeList.add(docCounter);

        SerialPipes serialPipe = new SerialPipes(pipeList);

        Iterator<Instance> iterator = serialPipe.newIteratorFrom(cvsIterator);

        int count = 0;

        // We aren't really interested in the instance itself,
        //  just the total feature counts.
        LOG.info("Getting stats to complete prune actions ..");
        while (iterator.hasNext()) {
            count++;
            if (count % 100000 == 0) {
                LOG.info("Docs analyzed: " + count);
            }
            iterator.next();
        }
        LOG.info("Prune stats collected");

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


    public Pipe buildMinimal() {
        ArrayList pipeList = new ArrayList();

        // Read data from File objects

        pipeList.add(new Input2CharSequence("UTF-8"));

        // Regular expression for what constitutes a token.
        //  This pattern includes Unicode letters, Unicode numbers,
        //   and the underscore character. Alternatives:
        //    "\\S+"   (anything not whitespace)
        //    "\\w+"    ( A-Z, a-z, 0-9, _ )
        //    "[\\p{L}\\p{N}_]+|[\\p{P}]+"   (a group of only letters and numbers OR
        //                                    a group of only punctuation marks)
        Pattern tokenPattern =
                Pattern.compile("[\\p{L}\\p{N}_]+");

        // Tokenize raw strings
        pipeList.add(new CharSequence2TokenSequence(tokenPattern));

//        pipeList.add(new TokenSequenceRemoveStopwords(false, false));
//
//        // Remove tokens that contain non-alphabetic characters
//        pipeList.add(new TokenSequenceRemoveNonAlpha(false));

        // Rather than storing tokens as strings, convert
        //  them to integers by looking them up in an alphabet.
        pipeList.add(new TokenSequence2FeatureSequence());

        // Print out the features and the label
//        pipeList.add(new PrintInputAndTarget());

        return new SerialPipes(pipeList);
    }

}
