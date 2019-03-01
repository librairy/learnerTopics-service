package org.librairy.service.learner.io;

import org.librairy.service.learner.facade.model.DataSource;
import org.librairy.service.learner.facade.model.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class ReaderFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ReaderFactory.class);

    public static Reader newFrom(DataSource dataSource) throws IOException {

        Format format = dataSource.getFormat();

        switch (format){
            case SOLR_CORE: return new SolrReader(dataSource);
            case CSV: return new CSVReader(dataSource, false);
            case CSV_TAR_GZ: return new CSVReader(dataSource, true);
            case JSONL: return new JsonlReader(dataSource, false);
            case JSONL_TAR_GZ: return new JsonlReader(dataSource, true);
            default: throw new RuntimeException("No reader found by hardFormat: " + format);
        }

    }

}
