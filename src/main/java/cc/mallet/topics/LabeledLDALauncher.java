package cc.mallet.topics;

import cc.mallet.types.FeatureSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelAlphabet;
import org.librairy.service.learner.builders.MailBuilder;
import org.librairy.service.modeler.service.TopicsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class LabeledLDALauncher {

    private static final Logger LOG = LoggerFactory.getLogger(LabeledLDALauncher.class);

    @Autowired
    CSVReader csvReader;

    @Autowired
    ModelLauncher modelLauncher;

    @Autowired
    TopicsService topicsService;

    @Autowired
    MailBuilder mailBuilder;

    public void setCsvReader(CSVReader csvReader) {
        this.csvReader = csvReader;
    }

    public void train(ModelParams parameters, String email) throws IOException {

        File outputDirFile = Paths.get(parameters.getOutputDir()).toFile();
        if (!outputDirFile.exists()) outputDirFile.mkdirs();

        Double alpha        = parameters.getAlpha();
        Double beta         = parameters.getBeta();
        Integer numTopWords = parameters.getNumTopWords();
        Integer numIterations = parameters.getNumIterations();
        String pos          = parameters.getPos();
        Integer maxRetries  = parameters.getNumRetries();


        LabeledLDA labeledLDA = new LabeledLDA(alpha, beta);

        parameters.getStopwords().forEach(word -> labeledLDA.addStop(word));

        //labeledLDA.setRandomSeed(100);

        Instant startProcess = Instant.now();

        InstanceList instances = csvReader.getParallelInstances(parameters.getCorpusFile(), parameters.getLanguage(), parameters.getRegEx(),parameters.getTextIndex(), parameters.getLabelIndex(), parameters.getIdIndex(),true,pos);


        LOG.info("Data loaded.");
        if(instances.size() > 0 && instances.get(0) != null) {
            Object e = ((Instance)instances.get(0)).getData();
            if(!(e instanceof FeatureSequence)) {
                LOG.warn("Topic modeling currently only supports feature sequences: use --keep-sequence option when importing data.");
                System.exit(1);
            }
        }

        labeledLDA.addInstances(instances);

        int size = instances.size();
        Instant endProcess = Instant.now();
        String durationProcess = ChronoUnit.HOURS.between(startProcess, endProcess) + "hours "
                + ChronoUnit.MINUTES.between(startProcess, endProcess) % 60 + "min "
                + (ChronoUnit.SECONDS.between(startProcess, endProcess) % 60) + "secs";
        LOG.info("Corpus ("+size+") processed in: " + durationProcess);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int parallelThreads = (availableProcessors > 1) && (size/availableProcessors >= 100)? availableProcessors -1: 1;
        LOG.info("Parallel model to: " + parallelThreads + " threads");
//        labeledLDA.setNumThreads(parallelThreads);

        //
        Integer intervalTopicDisplay = numIterations/2;
        labeledLDA.setTopicDisplay(intervalTopicDisplay, numTopWords);
        LOG.info("Interval Topic Display: " + intervalTopicDisplay);

        Integer intervalTopicValidation = numIterations/2;
        labeledLDA.setTopicValidation(intervalTopicValidation,10);
        labeledLDA.maxRetries = maxRetries;
        LOG.info("Interval Topic Validation: " + intervalTopicValidation);

        labeledLDA.setNumIterations(numIterations);
        LOG.info("Num Iterations: " + numIterations);

        LOG.info("building labeled topic model " + parameters);
        Instant startModel = Instant.now();
        labeledLDA.estimate();


        Instant endModel = Instant.now();
        String durationModel = ChronoUnit.HOURS.between(startModel, endModel) + "hours "
                + ChronoUnit.MINUTES.between(startModel, endModel) % 60 + "min "
                + (ChronoUnit.SECONDS.between(startModel, endModel) % 60) + "secs";
        LOG.info("Topic Model created in: " + durationModel);

        LOG.info("Calculating logLikelihood...");
        double loglikelihood = labeledLDA.modelLogLikelihood();
        LOG.info("logLikelihood = " + loglikelihood);

        //
        ParallelTopicModel parallelModel = new ParallelTopicModel(labeledLDA.getTopicAlphabet(), alpha * (double)labeledLDA.numTopics, beta);
        parallelModel.data = labeledLDA.data;
        parallelModel.alphabet = labeledLDA.alphabet;
        parallelModel.numTypes = labeledLDA.numTypes;
        parallelModel.betaSum = labeledLDA.betaSum;
        parallelModel.stoplist = labeledLDA.stoplist;

        LabelAlphabet labelAlphabet = new LabelAlphabet();
        for(int i=0; i<labeledLDA.labelAlphabet.size();i++){
            labelAlphabet.lookupIndex(labeledLDA.labelAlphabet.lookupObject(i),true);
        }


        parallelModel.topicAlphabet = labelAlphabet;
        parallelModel.buildInitialTypeTopicCounts();


        LOG.info("saving model to disk .. ");
        modelLauncher.saveModel(parameters.getOutputDir(), "llda",parameters, parallelModel, numTopWords);

        mailBuilder.newMailTo(email);

    }
}
