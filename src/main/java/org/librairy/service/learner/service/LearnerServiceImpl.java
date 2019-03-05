package org.librairy.service.learner.service;

import org.apache.avro.AvroRemoteException;
import org.librairy.service.learner.facade.model.LearnerService;
import org.librairy.service.learner.facade.model.Result;
import org.librairy.service.learner.facade.model.TopicsRequest;
import org.springframework.stereotype.Component;

@Component
public class LearnerServiceImpl implements LearnerService {
    @Override
    public Result createTopics(TopicsRequest topicsRequest) throws AvroRemoteException {
        return null;
    }

    @Override
    public Result cleanCache() throws AvroRemoteException {
        return null;
    }

//    private static final Logger LOG = LoggerFactory.getLogger(LearnerServiceImpl.class);
//
//    @Autowired
//    CorpusBuilder corpusService;
//
//    @Autowired
//    TopicsService topicsService;
//
//    @Autowired
//    LibrairyNlpClient librairyNlpClient;
//
//    @Autowired
//    TrainingPoolManager trainingPoolManager;
//
//    private ParallelExecutor executor;
//
//
//    @PostConstruct
//    public void setup() throws IOException {
//
//        //// Load resources
//        //model              = Paths.take(resourceFolder,"resource.bin").toFile().getAbsolutePath();
//
//        executor             = new ParallelExecutor();
//        LOG.info("Service initialized");
//    }
//
//
//    @Override
//    public String addDocument(Document document, boolean multigrams, boolean raw) throws AvroRemoteException {
//
//        executor.submit(() -> {
//            try {
//                document.setLabels(document.getLabels().stream().map(label -> label.replace(" ","_")).collect(Collectors.toList()));
//                corpusService.add(document,multigrams,raw);
//            } catch (Exception e) {
//                LOG.error("IO Error",e);
//            }
//        });
//        return "document added";
//    }
//
//    @Override
//    public String reset() throws AvroRemoteException {
//        try {
//            corpusService.remove();
//            topicsService.remove();
//            librairyNlpClient.shutdown();
//        } catch (IOException e) {
//            throw new AvroRemoteException("IO Error",e);
//        }
//        return "documents deleted";
//    }
//
//    @Override
//    public String train(Map<String, String> map) throws AvroRemoteException {
//
//        LOG.info("Training a new model from parameters: " + map + " with a corpus of " + corpusService.getNumDocs() + " docs");
//        if (trainingPoolManager.train(map)){
//            topicsService.remove();
//            return "building a new model";
//        }
//        else return "There is currently a model training";
//    }
//
//    @Override
//    public Corpus getCorpus() throws AvroRemoteException {
//        return Corpus.newBuilder().setSize(Long.valueOf(corpusService.getNumDocs()).intValue()).setUpdated(corpusService.getUpdated()).build();
//    }
}
