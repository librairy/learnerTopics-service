package org.librairy.service.learner.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Document {

    private static final Logger LOG = LoggerFactory.getLogger(Document.class);

    private String id;

    private String text;

    private List<String> labels = new ArrayList<>();

    public Document() {
    }

    public Document(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public Document(String id, String text, List<String> labels) {
        this.id = id;
        this.text = text;
        this.labels = labels;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
}
