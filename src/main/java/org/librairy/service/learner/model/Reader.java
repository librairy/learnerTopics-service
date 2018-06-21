package org.librairy.service.learner.model;

import cc.mallet.pipe.iterator.CsvIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Reader {

    private static final Logger LOG = LoggerFactory.getLogger(Reader.class);

    CsvIterator iterator;

    BufferedReader bufferedReader;

    public Reader(CsvIterator iterator, BufferedReader bufferedReader) {
        this.iterator = iterator;
        this.bufferedReader = bufferedReader;
    }

    public void close(){
        try {
            this.bufferedReader.close();
        } catch (IOException e) {
           LOG.error("Unexpected error: " + e.getMessage());
        }
    }

    public CsvIterator getIterator() {
        return iterator;
    }

    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }
}
