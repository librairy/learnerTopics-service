package cc.mallet.topics;

import java.io.Serializable;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class LDAParameters implements Serializable{

    Double alpha            = 0.1;

    Double beta             = 0.001;

    Integer numTopics       = 10;

    Integer numTopWords     = 50;

    Integer numIterations   = 1000;

    String corpusFile;

    String outputDir;

    String language         = "en";

    String regEx            = "(.*);;(.*);;(.*);;(.*)";

    Integer textIndex       = 4;

    Integer labelIndex      = 3;

    Integer idIndex         = 1;

    public LDAParameters(String corpusFile, String outputDir) {
        this.corpusFile = corpusFile;
        this.outputDir = outputDir;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Double getAlpha() {
        return alpha;
    }

    public void setAlpha(Double alpha) {
        this.alpha = alpha;
    }

    public Double getBeta() {
        return beta;
    }

    public void setBeta(Double beta) {
        this.beta = beta;
    }

    public Integer getNumTopics() {
        return numTopics;
    }

    public void setNumTopics(Integer numTopics) {
        this.numTopics = numTopics;
    }

    public Integer getNumTopWords() {
        return numTopWords;
    }

    public void setNumTopWords(Integer numTopWords) {
        this.numTopWords = numTopWords;
    }

    public Integer getNumIterations() {
        return numIterations;
    }

    public void setNumIterations(Integer numIterations) {
        this.numIterations = numIterations;
    }

    public String getCorpusFile() {
        return corpusFile;
    }

    public void setCorpusFile(String corpusFile) {
        this.corpusFile = corpusFile;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getRegEx() {
        return regEx;
    }

    public void setRegEx(String regEx) {
        this.regEx = regEx;
    }

    public Integer getTextIndex() {
        return textIndex;
    }

    public void setTextIndex(Integer textIndex) {
        this.textIndex = textIndex;
    }

    public Integer getLabelIndex() {
        return labelIndex;
    }

    public void setLabelIndex(Integer labelIndex) {
        this.labelIndex = labelIndex;
    }

    public Integer getIdIndex() {
        return idIndex;
    }

    public void setIdIndex(Integer idIndex) {
        this.idIndex = idIndex;
    }

    @Override
    public String toString() {
        return "LDAParameters{" +
                "alpha=" + alpha +
                ", beta=" + beta +
                ", numTopics=" + numTopics +
                ", numTopWords=" + numTopWords +
                ", numIterations=" + numIterations +
                '}';
    }
}
