package org.librairy.service.learner.service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class TimeService {

    private static SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ssZ");

    public static String now(){
        return formatter.format(new Date());
    }

    public static String from(Long timestamp){
        return formatter.format(timestamp);
    }
}
