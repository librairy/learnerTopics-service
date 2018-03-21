package cc.mallet.topics;

import cc.mallet.types.InstanceList;
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
public class LDALauncher {

    private static final Logger LOG = LoggerFactory.getLogger(LDALauncher.class);

    @Autowired
    CSVReader csvReader;

    @Autowired
    ModelLauncher modelLauncher;

    @Autowired
    TopicsService topicsService;

    public void setCsvReader(CSVReader csvReader) {
        this.csvReader = csvReader;
    }

    public void train(LDAParameters parameters) throws IOException {

        File outputDirFile = Paths.get(parameters.getOutputDir()).toFile();
        if (!outputDirFile.exists()) outputDirFile.mkdirs();

        Double alpha        = parameters.getAlpha();
        Double beta         = parameters.getBeta();
        int numTopics       = parameters.getNumTopics();
        Integer numTopWords = parameters.getNumTopWords();
        Integer numIterations = parameters.getNumIterations();



        //labeledLDA.setRandomSeed(100);
        LOG.info("processing corpus: " + parameters.getCorpusFile());

        Instant startProcess = Instant.now();

        InstanceList instances = csvReader.getParallelInstances(parameters.getCorpusFile(), parameters.getLanguage(), parameters.getRegEx(),parameters.getTextIndex(), parameters.getLabelIndex(), parameters.getIdIndex());


        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        ParallelTopicModel model = new ParallelTopicModel(numTopics, numTopics*alpha, beta);

        model.addInstances(instances);

        int size = instances.size();
        Instant endProcess = Instant.now();
        String durationProcess = ChronoUnit.HOURS.between(startProcess, endProcess) + "hours "
                + ChronoUnit.MINUTES.between(startProcess, endProcess) % 60 + "min "
                + (ChronoUnit.SECONDS.between(startProcess, endProcess) % 60) + "secs";
        LOG.info("Corpus processed in: " + durationProcess);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int parallelThreads = (availableProcessors > 1) && (size/availableProcessors >= 100)? availableProcessors -1: 1;
        LOG.info("Parallel model to: " + parallelThreads + " threads");
        model.setNumThreads(parallelThreads);



        // Disable print loglikelihood. (use for testing purposes)
        model.printLogLikelihood = false;

        // Run the model for 50 iterations and stop (this is for testing only,
        //  for real applications, use 1000 to 2000 iterations)

        Integer optimizeInterval = numIterations/2;
        model.setOptimizeInterval(optimizeInterval);
        LOG.info("Optimize Interval: " + optimizeInterval);

        Integer intervalTopicDisplay = numIterations/2;
        model.setTopicDisplay(intervalTopicDisplay,5);
        LOG.info("Interval Topic Display: " + intervalTopicDisplay);

        model.setNumIterations(numIterations);
        LOG.info("Num Iterations: " + numIterations);

        LOG.info("building topic model " + parameters);
        Instant startModel = Instant.now();
        model.estimate();

        // Show the words and topics in the first instance

        // The data alphabet maps word IDs to strings
//        Alphabet dataAlphabet = instances.getDataAlphabet();
//
//        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
//        LabelSequence topics = model.getData().get(0).topicSequence;

//        Formatter out = new Formatter(new StringBuilder(), Locale.US);
//        for (int position = 0; position < tokens.getLength(); position++) {
//            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
//        }
//        System.out.println(out);


//        // Estimate the topic distribution of the first instance,
//        //  given the current Gibbs state.
//        double[] topicDistribution = model.getTopicProbabilities(0);
//
//        // Get an array of sorted sets of word ID/count pairs
//        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
//
//
//        // Show top 5 words in topics with proportions for the first document
//        for (int topic = 0; topic < numTopics; topic++) {
//            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
//
//            out = new Formatter(new StringBuilder(), Locale.US);
//            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
//            int rank = 0;
//            while (iterator.hasNext() && rank < 5) {
//                IDSorter idCountPair = iterator.next();
//                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
//                rank++;
//            }
//            System.out.println(out);
//        }

        Instant endModel = Instant.now();
        String durationModel = ChronoUnit.HOURS.between(startModel, endModel) + "hours "
                + ChronoUnit.MINUTES.between(startModel, endModel) % 60 + "min "
                + (ChronoUnit.SECONDS.between(startModel, endModel) % 60) + "secs";
        LOG.info("Topic Model created in: " + durationModel);


        LOG.info("saving model to disk .. ");
        modelLauncher.saveModel(parameters.getOutputDir(), parameters, model, numTopWords);

        try {
            topicsService.loadModel();
        } catch (Exception e) {
            LOG.error("Error loading the new model",e);
        }
    }


}
