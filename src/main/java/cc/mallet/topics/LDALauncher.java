package cc.mallet.topics;

import cc.mallet.types.InstanceList;
import org.librairy.service.learner.builders.InstanceBuilder;
import org.librairy.service.learner.builders.MailBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.zip.GZIPOutputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class LDALauncher {

    private static final Logger LOG = LoggerFactory.getLogger(LDALauncher.class);

    @Value("#{environment['OUTPUT_DIR']?:'${output.dir}'}")
    String outputDir;

    @Autowired
    InstanceBuilder instanceBuilder;

    @Autowired
    ModelLauncher modelLauncher;

    @Autowired
    MailBuilder mailBuilder;


    public void train(ModelParams parameters, String email) throws IOException {

        File outputDirFile = Paths.get(parameters.getOutputDir()).toFile();
        if (!outputDirFile.exists()) outputDirFile.mkdirs();

        Double alpha        = parameters.getAlpha();
        Double beta         = parameters.getBeta();
        int numTopics       = parameters.getNumTopics();
        Integer numTopWords = parameters.getNumTopWords();
        Integer numIterations = parameters.getNumIterations();
        String pos          = parameters.getPos();
        Integer maxRetries  = parameters.getNumRetries();
        Boolean raw         = parameters.getRaw();



        //labeledLDA.setRandomSeed(100);
        LOG.info("processing corpus: " + parameters.getCorpusFile());

        Instant startProcess = Instant.now();

        InstanceList instances = instanceBuilder.getInstances(parameters.getCorpusFile(), parameters.getRegEx(), parameters.getTextIndex(), parameters.getLabelIndex(), parameters.getIdIndex(), false, pos, parameters.getMinFreq(), parameters.getMaxDocRatio(),raw);

        int numWords = instances.getDataAlphabet().size();
        if ( numWords <= 10){
            LOG.warn("Not enough words ("+numWords+") to train a model. Task aborted");
            return;
        }

        ParallelTopicModel model = new ParallelTopicModel(numTopics, numTopics*alpha, beta);

        parameters.getStopwords().forEach(word -> model.addStop(word));

        model.addInstances(instances);

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


        Integer intervalTopicValidation = numIterations/2;
        model.setTopicValidation(intervalTopicValidation,10);
        model.maxRetries = maxRetries;
        LOG.info("Interval Topic Validation: " + intervalTopicValidation);

        model.setNumIterations(numIterations);
        LOG.info("Num Iterations: " + numIterations);

        LOG.info("building topic model " + parameters);
        Instant startModel = Instant.now();
        model.estimate();

        Instant endModel = Instant.now();
        String durationModel = ChronoUnit.HOURS.between(startModel, endModel) + "hours "
                + ChronoUnit.MINUTES.between(startModel, endModel) % 60 + "min "
                + (ChronoUnit.SECONDS.between(startModel, endModel) % 60) + "secs";
        LOG.info("Topic Model created in: " + durationModel);


        LOG.info("saving model to disk .. ");
        modelLauncher.saveModel(parameters.getOutputDir(), "lda", parameters, model, numTopWords);

        LOG.info("saving doctopics to disk .. ");
        File docTopicsFile = Paths.get(outputDir, "doctopics.csv.gz").toFile();
        if (docTopicsFile.exists()) docTopicsFile.delete();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(docTopicsFile, true))));

        model.printDenseDocumentTopicsAsCSV(new PrintWriter(writer));
        LOG.info("doctopics file created at: " + docTopicsFile.getAbsolutePath());
        writer.close();

        mailBuilder.newMailTo(email);

        LOG.info(" Model created and saved successfully");

    }


}
