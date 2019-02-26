package org.librairy.service.learner.io;

import org.librairy.service.learner.model.Document;

import java.util.Optional;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public interface Reader {

    Optional<Document> next();

    void offset(Integer numLines);

}

