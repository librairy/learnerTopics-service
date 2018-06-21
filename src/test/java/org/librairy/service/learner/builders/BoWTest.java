package org.librairy.service.learner.builders;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SimpleTokenizer;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import org.junit.Test;
import org.librairy.service.nlp.facade.model.PoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class BoWTest {

    private static final Logger LOG = LoggerFactory.getLogger(BoWTest.class);


    @Test
    public void minimal() throws IOException {

        Pipe pipe = new PipeBuilder().buildMinimal();
        InstanceList instances = new InstanceList(pipe);

        String regEx            = "(.*);;(.*);;(.*);;(.*)";

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("src/test/resources/corpus-words.csv")));

        Integer dataGroup   = 4;
        Integer targetGroup = 3;
        Integer uriGroup    = 1;
        CsvIterator iterator = new CsvIterator(reader, regEx, dataGroup, targetGroup, uriGroup);

        // Now process each instance provided by the iterator.
        instances.addThruPipe(iterator);

        reader.close();

        Iterator<Instance> it = instances.iterator();
        while(it.hasNext()){
            Instance instance = it.next();
            LOG.info("Instance: " + instance.getData());
        }

        ParallelTopicModel model = new ParallelTopicModel(2, 2*0.1, 0.001);

        model.addInstances(instances);

        model.estimate();

    }

    @Test
    public void bow() throws IOException {

        Pipe pipe = new PipeBuilder().build("NOUN VERB", false, new TokenSequenceRemoveStopwords());
        InstanceList instances = new InstanceList(pipe);

        String regEx            = "(.*);;(.*);;(.*);;(.*)";

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("src/test/resources/corpus-bow.csv")));

        Integer dataGroup   = 4;
        Integer targetGroup = 3;
        Integer uriGroup    = 1;
        CsvIterator iterator = new CsvIterator(reader, regEx, dataGroup, targetGroup, uriGroup);

        // Now process each instance provided by the iterator.
        instances.addThruPipe(iterator);

        reader.close();

        Iterator<Instance> it = instances.iterator();
        while(it.hasNext()){
            Instance instance = it.next();
            LOG.info("Instance: " + instance.getData());
        }

        ParallelTopicModel model = new ParallelTopicModel(2, 2*0.1, 0.001);

        model.addInstances(instances);

        model.estimate();


    }

}
