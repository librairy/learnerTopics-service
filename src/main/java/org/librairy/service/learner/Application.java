package org.librairy.service.learner;

import org.librairy.service.modeler.controllers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

//@ComponentScan({"org.librairy.service.learner","org.librairy.service.modeler.clients","org.librairy.service.modeler.service","cc.mallet.topics"})
@SpringBootApplication
@ComponentScan(basePackages = {"org.librairy.service","cc.mallet.topics"}, excludeFilters={
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value=ModelerAvroController.class),
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value=RestClassificationController.class),
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value=RestInferencesController.class),
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value=RestSettingsController.class),
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value=RestTopicsController.class)
})
public class Application  {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        new SpringApplication(Application.class).run(args);
    }

}
