package org.librairy.service.learner;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class RegExTest {

    private static final Logger LOG = LoggerFactory.getLogger(RegExTest.class);

    @Test
    public void validate(){

        String valid    = "Asunción_Gómez-Pérez";
        String invalid  = "l!!!-¡¡¡";

        //Pattern regex = Pattern.compile("[a-zA-Z0-9._-áéíóúÁÉÍÓÚ]+");
        Pattern pattern = Pattern.compile("[A-Za-z0-9-.@_~#áéíóúÁÉÍÓÚ]+");

        boolean v1 = pattern.matcher(valid).matches();
        boolean v2 = pattern.matcher(invalid).matches();
        System.out.println(v1);
        System.out.println(v2);
        Matcher m = pattern.matcher(valid);
        boolean result1 = m.find();
        boolean result2 = m.find();
        System.out.println("hi");


        boolean out = valid.matches("[A-Za-z0-9-.@_~#áéíóúÁÉÍÓÚ]");
        System.out.println(out);

        final String regex = "[A-Za-z0-9-.@_~#áéíóúÁÉÍÓÚ]+";
        final String string = "Asunción_Gómez-Pérez";

        final Pattern pattern2 = Pattern.compile(regex);
        final Matcher matcher = pattern2.matcher(string);
        boolean is = pattern2.matcher(string).matches();
        System.out.println(is);


    }

}
