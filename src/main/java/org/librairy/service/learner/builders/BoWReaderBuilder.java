package org.librairy.service.learner.builders;

import cc.mallet.pipe.iterator.CsvIterator;
import org.librairy.service.learner.model.BoWReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class BoWReaderBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(BoWReaderBuilder.class);


    public BoWReader fromCSV(String filePath, String regEx, int textIndex, int labelIndex, int idIndex) throws IOException {
        int dataGroup           = textIndex;
        int targetGroup         = labelIndex;
        int uriGroup            = idIndex;

        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filePath)), "UTF-8"));

        CsvIterator csvIterator = new CsvIterator(reader, regEx, dataGroup, targetGroup, uriGroup);

        return new BoWReader(csvIterator, reader);

    }

}
