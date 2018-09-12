package org.librairy.service.learner.builders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class PipeBuilderFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PipeBuilderFactory.class);

    public static PipeBuilderI newInstance(Integer size, Boolean raw){
        if (raw) return new RawPipeBuilder(size);
        else return new BoWPipeBuilder(size);
    }

}
