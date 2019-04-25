package org.librairy.service.learner;

import es.upm.oeg.librairy.service.modeler.controllers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

//@ComponentScan({"org.librairy.service.learner","org.librairy.service.modeler.clients","org.librairy.service.modeler.service","cc.mallet.topics"})
@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, SolrAutoConfiguration.class})
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
