package org.librairy.service.learner.model;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class AnnotationRequest {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationRequest.class);

    private String contactEmail;

    private String model;

    private String collection;

    private String filter;

    public AnnotationRequest(String model, String collection, String filter) {
        this.model = model;
        this.collection = collection;
        this.filter = filter;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public AnnotationRequest() {
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public boolean isValid(){
        return !Strings.isNullOrEmpty(model) && !Strings.isNullOrEmpty(collection);
    }
}
