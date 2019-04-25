package cc.mallet.topics;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.*;
import org.librairy.service.learner.builders.InstanceBuilder;
import org.librairy.service.learner.builders.MailBuilder;
import org.librairy.service.learner.model.TopicReport;
import org.librairy.service.learner.service.StoplabelService;
import org.librairy.service.learner.service.StopwordService;
import es.upm.oeg.librairy.service.modeler.facade.model.TopicWord;
import es.upm.oeg.librairy.service.modeler.service.TopicsService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class LabeledLDALauncher {

    private static final Logger LOG = LoggerFactory.getLogger(LabeledLDALauncher.class);

    @Value("#{environment['OUTPUT_DIR']?:'${output.dir}'}")
    String outputDir;

    @Autowired
    InstanceBuilder instanceBuilder;

    @Autowired
    ModelLauncher modelLauncher;

    @Autowired
    MailBuilder mailBuilder;

    @Autowired
    TopicsService topicsService;

    @Autowired
    StoplabelService stoplabelService;

    @Autowired
    StopwordService stopwordService;

    public void train(ModelParams parameters, String email) throws IOException {

        File outputDirFile = Paths.get(parameters.getOutputDir()).toFile();
        if (!outputDirFile.exists()){
            outputDirFile.mkdirs();
        }

        ParallelTopicModel model = null;
        Pipe pipe = null;
        Boolean isReady = false;

        List<String> stoplabels = new ArrayList<>(parameters.getStoplabels());
        List<String> stopwords  = new ArrayList<>(parameters.getStopwords());
        try{
            int swRetries = 0;
            do{

                TopicReport report = build(parameters);

                if (report.isEmpty()) return;

                model = report.getModel();

                pipe = report.getPipe();

                if (model.getNumTopics() < 2) break;

                Map<Integer, List<TopicWord>> topWords = topicsService.getTopWords(model, parameters.getNumTopWords());

                Map<String,List<String>> topics = new HashMap<>();

                for (Map.Entry<Integer,List<TopicWord>> entry : topWords.entrySet()){

                    Label label = model.topicAlphabet.lookupLabel(entry.getKey().intValue());

                    List<String> words = entry.getValue().stream().map(tw -> tw.getValue()).collect(Collectors.toList());
                    topics.put(label.toString(), words);

                }

                List<String> stoplabelCandidateList = stoplabelService.detect(topics, parameters.getNumTopWords());

                if (stoplabelCandidateList.isEmpty()){

                    List<String> stopWordCandidateList = stopwordService.detect(topics);

                    if (stopWordCandidateList.isEmpty()) break;

                    LOG.warn("Stop-words detected: " + stopWordCandidateList.size());
                    LOG.info("Total stop-word list size: " + stopwords.size());

                    swRetries = (stopWordCandidateList.size() == 1)? swRetries+1 : 0;
                    if (swRetries >=20) break;//break-point

                    stopwords.addAll(stopWordCandidateList);

                    parameters.setStopwords(stopwords);

                    continue;

                }

                LOG.warn("Invalid Topics detected: " + stoplabelCandidateList.size());
                LOG.info("Total stop-label list size: " + stoplabels.size());
                swRetries = 0;

                stoplabels.addAll(stoplabelCandidateList);
                parameters.setStoplabels(stoplabels);


            }while(!isReady);


            LOG.info("saving model to disk .. ");
            modelLauncher.saveModel(parameters.getOutputDir(), "llda",parameters, model, parameters.getNumTopWords(), pipe);

            LOG.info(" Model created and saved successfully");

        }catch (Exception e){
            LOG.error("Error creating topic model", e);
        }
    }

    public TopicReport build(ModelParams parameters) throws IOException {

        Double alpha        = parameters.getAlpha();
        Double beta         = parameters.getBeta();
        Integer numTopWords = parameters.getNumTopWords();
        Integer numIterations = parameters.getNumIterations();
        String pos          = parameters.getPos();
        Integer maxRetries  = parameters.getNumRetries();
        Boolean raw         = parameters.getRaw();
        Integer seed        = parameters.getSeed();
        Integer corpusSize  = parameters.getSize();

        LabeledLDA labeledLDA = new LabeledLDA(alpha, beta);

        labeledLDA.setRandomSeed(seed);

        Instant startProcess = Instant.now();

        InstanceList instances = instanceBuilder.getInstances(parameters.getCorpusFile(), corpusSize , parameters.getRegEx(), parameters.getTextIndex(), parameters.getLabelIndex(), parameters.getIdIndex(), true, pos, parameters.getMinFreq(), parameters.getMaxDocRatio(),raw, parameters.getStopwords(), parameters.getStoplabels());

        int numWords = instances.getDataAlphabet().size();
        if ( numWords <= 10){
            LOG.warn("Not enough words ("+numWords+") to train a model. Task aborted");
            return new TopicReport();
        }

        LOG.info("Instances created");
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



        ParallelTopicModel parallelModel = new ParallelTopicModel(labeledLDA.topicAlphabet, labeledLDA.alpha * labeledLDA.numTopics, labeledLDA.beta);
        parallelModel.data                              = labeledLDA.data;
        parallelModel.alphabet                          = labeledLDA.alphabet;
        parallelModel.numTypes                          = labeledLDA.numTypes;
        parallelModel.betaSum                           = labeledLDA.betaSum;
        parallelModel.numTopics                         = labeledLDA.numTopics;
        parallelModel.stoplist                          = labeledLDA.stoplist;
        parallelModel.tokensPerTopic                    = labeledLDA.tokensPerTopic;
        parallelModel.typeTopicCounts                   = labeledLDA.typeTopicCounts;
        parallelModel.maxRetries                        = labeledLDA.maxRetries;
        parallelModel.numIterations                     = labeledLDA.numIterations;
        parallelModel.showTopicsInterval                = labeledLDA.showTopicsInterval;
        parallelModel.wordsPerTopic                     = labeledLDA.wordsPerTopic;
        parallelModel.validateTopicsInterval            = labeledLDA.validateTopicsInterval;
        parallelModel.randomSeed                        = parameters.getSeed();

        LabelAlphabet labelAlphabet = new LabelAlphabet();
        for(int i=0; i<labeledLDA.labelAlphabet.size();i++){
            labelAlphabet.lookupIndex(labeledLDA.labelAlphabet.lookupObject(i),true);
        }

        parallelModel.topicAlphabet = labelAlphabet;

        parallelModel.buildInitialTypeTopicCounts();


        return new TopicReport(parallelModel, instances.getPipe());
    }
}
