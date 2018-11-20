package cc.mallet.topics;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.types.*;
import com.google.common.primitives.Doubles;
import org.apache.avro.AvroRemoteException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.librairy.service.learner.Application;
import org.librairy.service.learner.builders.BoWPipeBuilder;
import org.librairy.service.learner.builders.InstanceBuilder;
import org.librairy.service.learner.builders.PipeBuilderFactory;
import org.librairy.service.learner.builders.ReaderBuilder;
import org.librairy.service.learner.facade.model.Document;
import org.librairy.service.learner.facade.model.LearnerService;
import org.librairy.service.learner.model.Reader;
import org.librairy.service.learner.service.CorpusService;
import org.librairy.service.modeler.clients.LibrairyNlpClient;
import org.librairy.service.modeler.facade.model.ModelerService;
import org.librairy.service.modeler.service.BoWService;
import org.librairy.service.nlp.facade.model.Group;
import org.librairy.service.nlp.facade.model.PoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class InferenceTest {

    @Value("#{environment['RESOURCE_FOLDER']?:'${resource.folder}'}")
    String resourceFolder;

    @Autowired
    CorpusService corpusService;

    @Autowired
    InstanceBuilder instanceBuilder;

    @Autowired
    LibrairyNlpClient client;

    @Autowired
    ModelerService modelerService;

    @Autowired
    LearnerService learnerService;

    @Autowired
    LabeledLDALauncher launcher;

    @Autowired
    ModelLauncher ldaLauncher;

    private static final Logger LOG = LoggerFactory.getLogger(InferenceTest.class);

    List<Document> documents = new ArrayList<>();

    @Before
    public void setup() throws AvroRemoteException {
        documents.add(Document.newBuilder().setId("d1").setName("d1").setLabels(Arrays.asList("news")).setText("3 of Hearts is the self-titled debut studio album by the American group 3 of Hearts, released on March 6, 2001, through the record label RCA Nashville. It is a teen pop and country music album, though according to some music critics, it leans more towards pop music. The album was managed by American producer Byron Gallimore; its marketing focused on the group's crossover appeal to teenagers and young adults. 3 of Hearts performed on a national tour sponsored by Seventeen magazine and another sponsored by Walmart, and the singers were featured in several marketing campaigns. Reviews of the album were mixed; some critics praised the group's vocals and public image, but others criticized the songs as generic and lacking an authentic country sound.").build());
        documents.add(Document.newBuilder().setId("d2").setName("d2").setLabels(Arrays.asList("film")).setText("The Shape of Water is a 2017 American fantasy drama film directed by Guillermo del Toro and written by del Toro and Vanessa Taylor.[3][4] It stars Sally Hawkins, Michael Shannon, Richard Jenkins, Doug Jones, Michael Stuhlbarg, and Octavia Spencer. Set in Baltimore in 1962, the plot follows a mute custodian at a high-security government laboratory who falls in love with a captured human-amphibian creature.").build());
        documents.add(Document.newBuilder().setId("d3").setName("d3").setLabels(Arrays.asList("film")).setText("Guillermo del Toro Gómez is a Mexican film director, screenwriter, producer, and novelist. In his filmmaking career, del Toro has alternated between Spanish-language dark fantasy pieces, such as the gothic horror films The Devil's Backbone (2001) and Pan's Labyrinth (2006), and more mainstream American action films, such as the vampire superhero action film Blade II (2002), the supernatural superhero film Hellboy (2004), its sequel Hellboy II: The Golden Army (2008), Trollhunters (2016) and the science fiction monster film Pacific Rim (2013). His 2017 fantasy film The Shape of Water received critical acclaim and won a Golden Lion at the 74th Venice International Film Festival as well as the Academy Award for Best Picture. Del Toro received an Academy Award for Best Director for the film, as well as the Golden Globe, BAFTA, Critics' Choice, and Directors Guild of America.").build());
        documents.add(Document.newBuilder().setId("d4").setName("d4").setLabels(Arrays.asList("film")).setText("Blade II is a 2002 American superhero film based on the fictional character of the same name from Marvel Comics. It is the sequel of the first film and the second part of the Blade film series, followed by Blade: Trinity. It was written by David S. Goyer, who also wrote the previous film, directed by Guillermo del Toro, and had Wesley Snipes returning as the lead character and producer.").build());
        documents.add(Document.newBuilder().setId("d5").setName("d5").setLabels(Arrays.asList("film")).setText("Blade: Trinity (also known as Blade III or Blade III: Trinity) is a 2004 American superhero film written, produced and directed by David S. Goyer, who also wrote the screenplays to Blade and Blade II. It stars Wesley Snipes, who also produced, in the title role based on the Marvel Comics character Blade alongside Ryan Reynolds, Jessica Biel, Kris Kristofferson, Dominic Purcell, Parker Posey and Triple H.").build());
        documents.add(Document.newBuilder().setId("d6").setName("d6").setLabels(Arrays.asList("news")).setText("A binary search algorithm is a method to determine the position of a target value within a sorted array (an ordered list). Binary search compares the target value to the middle element of the array. If they are not equal, the half in which the target cannot lie is eliminated and the search continues on the remaining half, again taking the middle element to compare to the target value, and so on.").build());
        documents.add(Document.newBuilder().setId("d7").setName("d7").setLabels(Arrays.asList("news")).setText("The 2012 Miller Superbike World Championship round was the sixth round of the 2012 Superbike World Championship season. It took place on the weekend of May 26–28, 2012 at Miller Motorsports Park, in Tooele, Utah, United States. The races were held on Memorial Day Monday.").build());
        documents.add(Document.newBuilder().setId("d8").setName("d8").setLabels(Arrays.asList("news")).setText("Though the magazine's focus for the first five years or so of its existence was experimental/underground music, its focus at the turn of the century broadened to include an emphasis on covering alt-country and indie acts such as Wilco, Steve Earle, The New Pornographers, The Shins, and even more established acts such as Tom Petty. Despite this, it still maintains a section devoted to free jazz and obscure electronic-based music in each issue. It has also done long articles on jazz icons Albert Ayler, Ken Vandermark, and Ornette Coleman. The photographic style of subjects has also evolved from inventive avant-garde settings to stark, no-frills closeups of band members. Magnet has paid much less attention over the years to metal and gangsta rap.").build());
    }

    @Test
    @Ignore
    public void execute() throws IOException {


        learnerService.reset();

        boolean multigram = false;

        for(Document doc: documents){
            corpusService.add(doc,multigram,false);
        }
        corpusService.close();

        ModelParams params = new ModelParams();
        params.setNumIterations(100);
        params.setInference(true);
        params.setPos("NOUN VERB ADJECTIVE");


        LabeledLDA labeledLDA = new LabeledLDA(0.1, 0.1);

        List<PoS> posList = Arrays.asList(PoS.NOUN, PoS.VERB, PoS.ADJECTIVE);
        String pos = posList.stream().map(p -> p.name()).collect(Collectors.joining(" "));

        String corpusPath = "/Users/cbadenes/Projects/librairy/public/learnerTopics-service/output/bows.csv.gz";

        InstanceList instances = instanceBuilder.getInstances(corpusPath, "(.*);;(.*);;(.*);;(.*)", 4, 3, 1, true, pos, 0, 1.0,false, Collections.emptyList());

        File iFile = new File("src/test/bin/model/instances.data");
        if (iFile.exists()) iFile.delete();
        instances.save(iFile);

        Alphabet al1 = instances.getAlphabet();
        Alphabet ald1 = instances.getDataAlphabet();
        List<Alphabet> als = instances.stream().map(i -> i.getAlphabet()).collect(Collectors.toList());


        labeledLDA.addInstances(instances);

        labeledLDA.setTopicDisplay(50, 10);

        labeledLDA.setNumIterations(100);

        labeledLDA.estimate();

        Alphabet ldaa = labeledLDA.getAlphabet();


        ParallelTopicModel topicModel = new ParallelTopicModel(labeledLDA.topicAlphabet, labeledLDA.alpha * labeledLDA.numTopics, labeledLDA.beta);
        topicModel.data                  = labeledLDA.data;
        topicModel.alphabet              = labeledLDA.alphabet;
        topicModel.numTypes              = labeledLDA.numTypes;
        topicModel.betaSum               = labeledLDA.betaSum;
        topicModel.numTopics             = labeledLDA.numTopics;
        topicModel.stoplist              = labeledLDA.stoplist;
        topicModel.tokensPerTopic        = labeledLDA.tokensPerTopic;
        topicModel.typeTopicCounts       = labeledLDA.typeTopicCounts;
        topicModel.maxRetries            = labeledLDA.maxRetries;
        topicModel.numIterations         = labeledLDA.numIterations;
        topicModel.showTopicsInterval    = labeledLDA.showTopicsInterval;
        topicModel.wordsPerTopic         = labeledLDA.wordsPerTopic;
        topicModel.validateTopicsInterval         = labeledLDA.validateTopicsInterval;
//
        LabelAlphabet labelAlphabet = new LabelAlphabet();
        for(int i=0; i<labeledLDA.labelAlphabet.size();i++){
            labelAlphabet.lookupIndex(labeledLDA.labelAlphabet.lookupObject(i),true);
        }

        topicModel.topicAlphabet = labelAlphabet;

        topicModel.buildInitialTypeTopicCounts();

        StringWriter out1 = new StringWriter();
        topicModel.printDenseDocumentTopics(new PrintWriter(out1));
        LOG.info("Training Set: " + out1);

        TopicInferencer inferencer = topicModel.getInferencer();


        Pipe previousPipe = instances.getPipe();

        for(Document doc: documents){

            String text = "Carlos and Peter are working in films";
            List<Group> bows = client.bow(text, "en", posList, multigram);

            String data = BoWService.toText(bows);
            String name = doc.getName();
            String target = doc.getLabels().get(0);

    //        InstanceList previousInstanceList = InstanceList.load(new File("src/test/bin/model/instances.data"));
    //        Pipe pipe = previousInstanceList.getPipe();

            Instance rawInstance = new Instance(data,target,name,null);


            Instance existInstance = instances.stream().filter(i -> i.getName().equals(doc.getName())).collect(Collectors.toList()).get(0);



//            Pipe pipe = PipeBuilderFactory.newInstance(raw).build(this.pos);

//            InstanceList auxInstances = new InstanceList(pipe);
            instances.addThruPipe(rawInstance);



            Alphabet ai = instances.getAlphabet();
            Alphabet a1 = rawInstance.getAlphabet();
            Alphabet a2 = existInstance.getAlphabet();


            //Pipe pipe =  new BoWPipeBuilder(10).build(pos, true, new TokenSequenceRemoveStopwords(false, false));
            Pipe pipe = new SerialPipes();
            pipe.setDataAlphabet(ldaa);
            pipe.setTargetAlphabet(labeledLDA.getTopicAlphabet());




            FeatureSequence data2 = (FeatureSequence) rawInstance.getData();
            FeatureSequence data3 = new FeatureSequence(a2);
            for( int i=0; i< data2.getLength(); i++){
                String feature = (String) data2.get(i);
                int i1 = a1.lookupIndex(feature, false);
                int i2 = a2.lookupIndex(feature, false);
                data3.add(feature);
                System.out.println(i1 + "-" + i2);
            }
            double[] td1 = inferencer.getSampledDistribution(new Instance(data3,target,name,null), 100, 1, 5);
            LOG.info("[x]Topic Distribution of: " + doc.getId() + ".. " + Arrays.toString(td1));



            InstanceList auxInstances = new InstanceList(pipe);
            auxInstances.addThruPipe(new Instance(BoWService.toText(bows),target,name,null));


            int thinning = 1;//1
            int burnIn = 5;//5
            double[] topicDistribution = inferencer.getSampledDistribution(auxInstances.get(0), 100, thinning, burnIn);

            LOG.info("[a]Topic Distribution of: " + doc.getId() + ".. " + Arrays.toString(topicDistribution));


            double[] td = inferencer.getSampledDistribution(existInstance, 100, thinning, burnIn);
            LOG.info("[b]Topic Distribution of: " + doc.getId() + ".. " + Arrays.toString(td));



//            InstanceList instList = new InstanceList(previousPipe);
//            Reader csvReader = new ReaderBuilder().fromCSV(corpusPath, "(.*);;(.*);;(.*);;(.*)", 4, 3, 1);
//            CsvIterator cvsIterator = csvReader.getIterator();
//            while(cvsIterator.hasNext()) {
//
//                Instance inst = cvsIterator.next();
//                instList.addThruPipe(inst);
//            }
//
//            for(Instance instance: instList){
//                double[] tdOut = inferencer.getSampledDistribution(instance, 100, thinning, burnIn);
//                LOG.info("[c]Topic Distribution of: " + instance.getName() + ".. " + Arrays.toString(tdOut));
//            }


        }

        File distFile = new File("output/dist.txt");
        inferencer.writeInferredDistributions(instances,distFile,100,1,5,0.0,100);

        Files.readAllLines(Paths.get(distFile.getAbsolutePath())).forEach(line -> LOG.info("Inference: " + line));


    }


    @Test
    @Ignore
    public void rawtext() throws Exception {
        String data = "nonhuman=6#ADJECTIVE# primate=6#NOUN# human=5#ADJECTIVE# representation=4#NOUN# brain=4#NOUN# datum=3#NOUN# project=3#NOUN# relate=3#VERB# computational_models=3#NOUN# rsa=3#NOUN# be=2#VERB# multivariate=2#ADJECTIVE# technique=2#NOUN# dissimilarity=2#NOUN# analysis=2#NOUN# matrix=2#NOUN# region=2#NOUN# object_recognition=2#NOUN# brain-activity=2#NOUN# poorly=1#ADVERB# compare=1#VERB# idea=1#NOUN# code=1#NOUN# activity=1#NOUN# representationally=1#ADVERB# study=1#NOUN# best=1#ADVERB# contribute=1#VERB# cell=1#NOUN# computational_neuroscience=1#NOUN# acquire=1#VERB# integral=1#ADJECTIVE# divide=1#VERB# similarity=1#NOUN# pattern=1#NOUN# recording=1#NOUN# moreover=1#ADVERB# key=1#ADJECTIVE# here=1#ADVERB# general=1#ADJECTIVE# more=1#ADVERB# means=1#NOUN# statistically=1#ADVERB# determine=1#VERB# visual=1#ADJECTIVE# pulation-code=1#ADJECTIVE# computation=1#NOUN# match=1#VERB# human_brain=1#NOUN# massively=1#ADVERB# form=1#VERB# call=1#VERB# freely=1#ADVERB# such=1#ADJECTIVE# characterize=1#VERB# component=1#NOUN# cortical=1#ADJECTIVE# stimulus-evoked=1#ADJECTIVE# novel=1#ADJECTIVE# representational=1#ADJECTIVE# challenge=1#NOUN# spatiotemporal=1#ADJECTIVE# further=1#ADVERB# homologous=1#ADJECTIVE# representational_content=1#NOUN# explain=1#VERB# approach=1#NOUN# available=1#ADJECTIVE# focus=1#NOUN# easy-to-use=1#ADJECTIVE# provide=1#VERB# empirical_data=1#NOUN# integrated_systems=1#NOUN# visualize=1#VERB# problem=1#NOUN# implication=1#NOUN# richly=1#ADVERB# major=1#ADJECTIVE# community=1#NOUN# matlab=1#NOUN# test=1#VERB# functional_magnetic_resonance_imaging_(fmri)=1#NOUN# neuroscience=1#NOUN# population=1#NOUN# understand=1#VERB# still=1#ADVERB# develop=1#VERB# exist=1#VERB# core=1#NOUN# bridge=1#NOUN# contrast=1#NOUN# fmrus=1#NOUN# theory=1#NOUN# toolbox=1#NOUN# tackle=1#VERB# animal_models=1#NOUN# give=1#VERB#";
        String name = "";
        String source = null;
        String target = "";

//        InstanceList previousInstanceList = InstanceList.load(new File("src/test/bin/model/instances.data"));
//        Pipe pipe = previousInstanceList.getPipe();

        Instance rawInstance = new Instance(data,target,name,source);

//        Pipe pipe = PipeBuilderFactory.newInstance(raw).build(this.pos);

        Pipe pipe = ldaLauncher.readModelPipe(resourceFolder);
        InstanceList instances = new InstanceList(pipe);
        instances.addThruPipe(rawInstance);

        // Use model alphabet to set features
        Alphabet alphabet = ldaLauncher.readModelAlphabet(resourceFolder);
        FeatureSequence fs = (FeatureSequence) rawInstance.getData();
        FeatureSequence featureData = new FeatureSequence(alphabet);
        for( int i=0; i< fs.getLength(); i++){
            String feature = (String) fs.get(i);
            featureData.add(feature);
        }

        int thinning = 1;//1
        int burnIn = 5;//5
        int iterations = 100;
        TopicInferencer topicInferer = ldaLauncher.getTopicInferencer(resourceFolder);
        double[] topicDistribution = topicInferer.getSampledDistribution(new Instance(featureData, target, name, source), iterations, thinning, burnIn);

        LOG.info("Topic Distribution : " + Arrays.toString(topicDistribution));
    }

}
