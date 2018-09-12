package org.librairy.service.learner.builders;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Instance;

import java.util.Iterator;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public interface PipeBuilderI {

    Pipe build(String pos, Boolean enableTarget, TokenSequenceRemoveStopwords stopWordTokenizer);

    void prune(Iterator<Instance> cvsIterator, TokenSequenceRemoveStopwords stopWordTokenizer, String pos, Integer minFreq, Double docProportionCutoff);

}
