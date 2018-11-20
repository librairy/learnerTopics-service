package org.librairy.service.learner.model;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.librairy.service.learner.metric.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicSummary {

    private static final Logger LOG = LoggerFactory.getLogger(TopicSummary.class);

    private final List<TopicPoint> groups;

    private final HashFunction hf = Hashing.murmur3_32();

    private static final String GROUP_SEPARATOR = "#";


    public TopicSummary(List<Double> topicDistribution) {
        double eps = new Stats(topicDistribution).getDev();
        this.groups = cluster(topicDistribution, eps);
        Collections.sort(this.groups, (a, b) -> -a.getScore().compareTo(b.getScore()));
    }

    public String getReducedHashTopicsBy(int num) {
        if (groups.size()<=num) return groups.subList(0,1).stream().map(tp -> tp.getId()).collect(Collectors.joining(GROUP_SEPARATOR));
        return groups.subList(0,groups.size()-num).stream().map(tp -> tp.getId()).collect(Collectors.joining(GROUP_SEPARATOR));
    }

    public Integer getReducedHashCodeBy(int num){
        if (groups.size()<=num) return hf.hashString(groups.subList(0,1).stream().map(tp -> tp.getId()).collect(Collectors.joining(GROUP_SEPARATOR)), Charset.defaultCharset()).asInt();
        return hf.hashString(groups.subList(0,groups.size()-num).stream().map(tp -> tp.getId()).collect(Collectors.joining(GROUP_SEPARATOR)), Charset.defaultCharset()).asInt();
    }

    public String getTopHashTopicsBy(int num) {
        if (groups.size()<=num) return getReducedHashTopicsBy(0);
        return groups.subList(0,num).stream().map(tp -> tp.getId()).collect(Collectors.joining(GROUP_SEPARATOR));
    }

    public Integer getTopHashCodeBy(int num){
        if (groups.size()<=num) return getReducedHashCodeBy(0);
        return hf.hashString(groups.subList(0,num).stream().map(tp -> tp.getId()).collect(Collectors.joining(GROUP_SEPARATOR)), Charset.defaultCharset()).asInt();
    }


    public String getHashExpression(){
        return this.groups.stream().map(tp -> tp.getId()).collect(Collectors.joining("\n"));
    }

    public int getSize(){
        return groups.size();
    }


    public static List<TopicPoint> cluster(List<Double> vector, double eps){
        DistanceMeasure distanceMeasure = new MonoDimensionalDistanceMeasure();

        int minPts = 0;

        DBSCANClusterer<TopicPoint> clusterer = new DBSCANClusterer<>(eps, minPts, distanceMeasure);


        List<TopicPoint> points = IntStream.range(0, vector.size()).mapToObj(i -> new TopicPoint("" + i, vector.get(i))).collect(Collectors.toList());
        List<Cluster<TopicPoint>> clusterList = clusterer.cluster(points);

        List<TopicPoint> groups = new ArrayList<>();
        int totalPoints = 0;
        for (Cluster<TopicPoint> cluster : clusterList) {
            Double score = (cluster.getPoints().stream().map(p -> p.getScore()).reduce((x, y) -> x + y).get()) / (cluster.getPoints().size());
            String label = cluster.getPoints().stream().map(p -> "t" + p.getId()).sorted((x, y) -> -x.compareTo(y)).collect(Collectors.joining("_"));

            totalPoints += cluster.getPoints().size();

            groups.add(new TopicPoint(label, score));
        }
        if (totalPoints < vector.size()) {
            List<TopicPoint> clusterPoints = clusterList.stream().flatMap(l -> l.getPoints().stream()).collect(Collectors.toList());
            List<TopicPoint> isolatedTopics = points.stream().filter(p -> !clusterPoints.contains(p)).collect(Collectors.toList());
            Double score = (isolatedTopics.stream().map(p -> p.getScore()).reduce((x, y) -> x + y).get()) / (isolatedTopics.size());
            String label = isolatedTopics.stream().map(p -> "t" + p.getId()).sorted((x, y) -> -x.compareTo(y)).collect(Collectors.joining("_"));
            groups.add(new TopicPoint(label, score));
        }
        Collections.sort(groups, (a, b) -> -a.getScore().compareTo(b.getScore()));
        return groups;
    }

    private static class MonoDimensionalDistanceMeasure implements DistanceMeasure {

        @Override
        public double compute(double[] p1, double[] p2) {
            return Math.abs(p1[0] - p2[0]);
        }
    }


}