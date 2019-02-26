package org.librairy.service.learner.service;

import org.librairy.service.learner.facade.model.TopicsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Component
public class MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);


    public void notifyError(TopicsRequest request, String message){
        LOG.info("Mail error notification of " + request + " because of " + message);
    }

    public void notifyCreation(TopicsRequest request, String message){
        LOG.info("Mail creation notification of " + request + " because of " + message);
    }



}
