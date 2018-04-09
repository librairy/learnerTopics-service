package cc.mallet.topics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class ModelFactory {

    @Autowired
    LDALauncher ldaLauncher;

    @Autowired
    LabeledLDALauncher lldaLauncher;


    public void train(Map<String,String> parameters, LDAParameters ldaParameters) throws IOException {


        if (!parameters.containsKey("algorithm")){
            ldaLauncher.train(ldaParameters);
        }else{

            switch (parameters.get("algorithm").toLowerCase()){
                case "lda":
                    ldaLauncher.train(ldaParameters);
                    break;
                case "llda":
                    lldaLauncher.train(ldaParameters);
                    break;
                default:
                    new RuntimeException("No algorithm found by parameter 'algorithm' = " + parameters.get("algorithm"));
            }
        }
    }
}
