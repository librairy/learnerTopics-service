package org.librairy.service.learner.controllers;

import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.avro.AvroRemoteException;
import org.librairy.service.learner.facade.model.LearnerService;
import org.librairy.service.learner.facade.rest.model.Corpus;
import org.librairy.service.learner.facade.rest.model.Document;
import org.librairy.service.learner.facade.rest.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/documents")
@Api(tags="/documents", description="corpus management")
public class RestDocumentsController {

    private static final Logger LOG = LoggerFactory.getLogger(RestDocumentsController.class);

    @Autowired
    LearnerService service;
    private Pattern labelPattern;

    @PostConstruct
    public void setup(){
        labelPattern = Pattern.compile("[A-Za-z0-9-.@_~#áéíóúÁÉÍÓÚñÑ]+");
    }

    @PreDestroy
    public void destroy(){

    }

    @ApiOperation(value = "add document to corpus", nickname = "postDocuments", response=String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = String.class),
    })
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<Result> add(@RequestBody Document document)  {
        try {
            if (document.getLabels() == null) document.setLabels(Collections.emptyList());

            if (document.getLabels().stream().filter(label -> !labelPattern.matcher(label).matches()).count() > 0) {
                LOG.warn("Invalid label values: " + document.getLabels());
                return new ResponseEntity(new Result("invalid label values. It should be:  " + labelPattern.pattern()),HttpStatus.BAD_REQUEST);
            }

            if (Strings.isNullOrEmpty(document.getText())){
                LOG.warn("Empty text: " + document.getId());
                return new ResponseEntity(new Result("empty text"),HttpStatus.BAD_REQUEST);
            }

            String result = service.addDocument(document);
            return new ResponseEntity(new Result(result), HttpStatus.CREATED);
        } catch (AvroRemoteException e) {
            return new ResponseEntity(new Result("internal service seems down"),HttpStatus.FAILED_DEPENDENCY);
        } catch (Exception e) {
            LOG.error("IO Error", e);
            return new ResponseEntity(new Result("IO error"),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "remove all documents", nickname = "deleteDocuments", response=String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Accepted", response = String.class),
    })
    @RequestMapping(method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<Result> removeAll()  {
        try {
            String result = service.reset();
            return new ResponseEntity(new Result(result), HttpStatus.ACCEPTED);
        } catch (AvroRemoteException e) {
            return new ResponseEntity(new Result("internal service seems down"),HttpStatus.FAILED_DEPENDENCY);
        } catch (Exception e) {
            LOG.error("IO Error", e);
            return new ResponseEntity(new Result("IO error"),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "statistics", nickname = "getDocuments", response=Corpus.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Corpus.class),
    })
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Corpus> get()  {
        try {
            return new ResponseEntity(new Corpus(service.getCorpus()), HttpStatus.OK);
        } catch (AvroRemoteException e) {
            return new ResponseEntity(new Result("internal service seems down"),HttpStatus.FAILED_DEPENDENCY);
        } catch (Exception e) {
            LOG.error("IO Error", e);
            return new ResponseEntity(new Result("IO error"),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
