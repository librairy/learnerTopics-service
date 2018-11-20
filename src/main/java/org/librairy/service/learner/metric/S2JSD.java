package org.librairy.service.learner.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class S2JSD {

    private static final Logger LOG = LoggerFactory.getLogger(S2JSD.class);

    public static Double distance(List<Double> v1, List<Double> v2) {
        return Math.sqrt(2.0 * JSD.distance(v1,v2));
    }

    public static Double similarity(List<Double> v1, List<Double> v2) {
        return 1-distance(v1,v2);
    }
}
