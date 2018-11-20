package org.librairy.service.learner.metric;

import cc.mallet.util.Maths;
import com.google.common.primitives.Doubles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class JSD  {

    private static final Logger LOG = LoggerFactory.getLogger(JSD.class);


    public static Double distance(List<Double> v1, List<Double> v2) {
        assert (v1.size() == v2.size());

        List<Double> avg = new ArrayList<>();
        for(int i=0;i<v1.size();i++){
            Double pq = v1.get(i) + v2.get(i);
            Double pq_2 = pq / 2.0;
            avg.add(pq_2);
        }

        return (0.5 * new KL().distance(v1,avg)) + (0.5 * new KL().distance(v2, avg));
    }

    public static Double similarity(List<Double> v1, List<Double> v2) {
        return 1-divergence(v1,v2);
    }

    private static double divergence(List<Double> v1, List<Double> v2) {
        return Maths.jensenShannonDivergence(Doubles.toArray(v1), Doubles.toArray(v2));

    }

}
