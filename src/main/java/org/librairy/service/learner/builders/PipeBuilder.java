package org.librairy.service.learner.builders;

import cc.mallet.pipe.*;
import com.google.common.base.Strings;
import org.librairy.service.modeler.clients.LibrairyNlpClient;
import org.librairy.service.nlp.facade.model.PoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class PipeBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(PipeBuilder.class);

    public PipeBuilder() {
    }

    public Pipe build(LibrairyNlpClient client, String language, String pos, Boolean enableTarget) {
        ArrayList pipeList = new ArrayList();

        // Read data from File objects

        pipeList.add(new Input2CharSequence("UTF-8"));

        List<PoS> posList = Arrays.asList(new PoS[]{PoS.NOUN, PoS.VERB, PoS.ADVERB, PoS.ADJECTIVE});

        if (!Strings.isNullOrEmpty(pos)){
            try{
                posList = Arrays.asList(pos.split(" ")).stream().map(i -> PoS.valueOf(i.toUpperCase())).collect(Collectors.toList());
            }catch (Exception e){
                LOG.warn("Invalid PoS values: " + pos);
            }
        }

        LOG.info("PoS: " + posList);

        pipeList.add(new Lemmatizer(client, language, posList));

        // Regular expression for what constitutes a token.
        //  This pattern includes Unicode letters, Unicode numbers,
        //   and the underscore character. Alternatives:
        //    "\\S+"   (anything not whitespace)
        //    "\\w+"    ( A-Z, a-z, 0-9, _ )
        //    "[\\p{L}\\p{N}_]+|[\\p{P}]+"   (a group of only letters and numbers OR
        //                                    a group of only punctuation marks)
        Pattern tokenPattern = Pattern.compile("[\\p{L}\\p{N}_]+");

        // Tokenize raw strings
        pipeList.add(new CharSequence2TokenSequence(tokenPattern));

//        // Normalize all tokens to all lowercase
//        pipeList.add(new TokenSequenceLowercase());

//        // Remove stopwords from a standard English stoplist.
//        //  options: [case sensitive] [mark deletions]
        pipeList.add(new TokenSequenceRemoveStopwords(false, false));

        // Rather than storing tokens as strings, convert
        //  them to integers by looking them up in an alphabet.
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
        // Print out the features and the label
//        pipeList.add(new PrintInputAndTarget());

        return new SerialPipes(pipeList);
    }

}
