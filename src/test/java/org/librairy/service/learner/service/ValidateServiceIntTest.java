package org.librairy.service.learner.service;

import org.apache.avro.AvroRemoteException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.librairy.service.learner.Application;
import org.librairy.service.learner.controllers.RestDocumentsController;
import org.librairy.service.learner.facade.rest.model.Document;
import org.librairy.service.learner.facade.rest.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class ValidateServiceIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateServiceIntTest.class);

    @Autowired
    RestDocumentsController controller;


    @Before
    public void setup() throws AvroRemoteException {
        //service.reset();
    }

    @After
    public void destroy() throws AvroRemoteException {
        //service.reset();
    }

    @Test
    public void train() throws IOException, InterruptedException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("output2/corpus.csv.gz"))));

        String line;
        while((line = reader.readLine()) != null){

            String[] values = line.split(";;");

            String id = values[0];
            String name = values[1];
            List<String> labels = Arrays.asList(values[2].split(" "));
            String text = "";
            if (values.length < 4){
                System.out.println("this is wrong");
            }else{
                text = values[3];
            }

            Pattern labelPattern = Pattern.compile("[A-Za-z0-9-.@_~#áéíóúÁÉÍÓÚñÑ]+");
            Boolean invalid = false;
            if (labels != null) {
                invalid = labels.stream().filter(label -> !labelPattern.matcher(label).matches()).count() > 0;
                if (invalid){
                    System.out.println("hi");
                }
            }


            Document document = new Document();
            document.setText(text);
            document.setId(id);
            document.setLabels(labels);
            document.setName(name);

            ResponseEntity<Result> response = controller.add(document);
            LOG.info("response: " +response);

        }


    }
}