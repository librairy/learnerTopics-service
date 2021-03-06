package org.librairy.service.learner.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.zip.GZIPOutputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class WriterUtils {

    private static final Logger LOG = LoggerFactory.getLogger(WriterUtils.class);

    public static BufferedWriter to(String path) throws IOException {
        File out = new File(path);
        if (out.exists()) out.delete();
        else {
            out.getParentFile().mkdirs();
        }
        return new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(path))));
    }

}
