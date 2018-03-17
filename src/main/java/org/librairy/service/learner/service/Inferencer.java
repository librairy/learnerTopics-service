package org.librairy.service.learner.service;

import cc.mallet.pipe.Pipe;
import cc.mallet.topics.LDALauncher;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import com.google.common.base.Strings;
import com.google.common.primitives.Doubles;
import org.librairy.service.learner.builders.PipeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Inferencer {
    private static final Logger LOG = LoggerFactory.getLogger(Inferencer.class);

    private final TopicInferencer topicInferer;
    private final LDALauncher ldaLauncher;
    private final String resourceFolder;
    private final LibrairyNlpClient client;
    private final String language;

    public Inferencer(LDALauncher ldaLauncher, LibrairyNlpClient client, String language, String resourceFolder) throws Exception {

        LOG.info("Initializing a new Inferer...");
        this.topicInferer               = ldaLauncher.getTopicInferencer(resourceFolder);
        this.ldaLauncher                = ldaLauncher;
        this.resourceFolder             = resourceFolder;
        this.client                     = client;
        this.language                   = language;
    }


    public List<Double> inference(String s) throws Exception {

        if (Strings.isNullOrEmpty(s)) return Collections.emptyList();

        String data = s;
        String name = "";
        String source = "";
        String target = "";
        Integer numIterations = 100;

        Instance rawInstance = new Instance(data,target,name,source);
        Pipe pipe = new PipeBuilder().build(client, language);
        InstanceList instances = new InstanceList(pipe);
        instances.addThruPipe(rawInstance);

        int thinning = 1;
        int burnIn = 0;
        double[] topicDistribution = topicInferer.getSampledDistribution(instances.get(0), numIterations, thinning, burnIn);


        LOG.debug("Topic Distribution of: " + s.substring(0,10)+ ".. " + Arrays.toString(topicDistribution));
        return Doubles.asList(topicDistribution);

    }
}