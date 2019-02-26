package org.librairy.service.learner.io;

import org.librairy.service.learner.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class SolrReader implements Reader {

    private static final Logger LOG = LoggerFactory.getLogger(SolrReader.class);

    @Override
    public Optional<Document> next() {
        return null;
    }

    @Override
    public void offset(Integer numLines) {

    }
}
