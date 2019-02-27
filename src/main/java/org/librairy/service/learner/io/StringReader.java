package org.librairy.service.learner.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class StringReader {

    private static final Logger LOG = LoggerFactory.getLogger(StringReader.class);

    public static String format(String raw){

        return raw.replaceAll("\\P{Print}", "")
                .replaceAll("[^a-zA-Z0-9 .,'_]", "");

    }


}
