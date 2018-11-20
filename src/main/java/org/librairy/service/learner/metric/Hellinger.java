package org.librairy.service.learner.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Hellinger  {

    private static final Logger LOG = LoggerFactory.getLogger(Hellinger.class);

    public static Double distance(List<Double> v1, List<Double> v2) {

        assert (v1.size() == v2.size());

        double sum = 0;
        for(int i=0; i<v1.size(); i++){

            double sqrtv1 = Math.sqrt(v1.get(i));
            double sqrtv2 = Math.sqrt(v2.get(i));

            double pow2 = Math.pow(sqrtv1 - sqrtv2, 2.0);
            sum += pow2;
        }

        double sqrtSum = Math.sqrt(sum);
        double multiplier = 1.0 / Math.sqrt(2.0);
        return multiplier*sqrtSum;
    }

    public static Double similarity(List<Double> v1, List<Double> v2) {
        return 1-distance(v1,v2);
    }
}
