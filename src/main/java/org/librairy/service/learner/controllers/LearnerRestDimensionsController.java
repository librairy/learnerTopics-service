package org.librairy.service.learner.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.avro.AvroRemoteException;
import org.librairy.service.learner.facade.model.LearnerService;
import org.librairy.service.learner.facade.rest.model.ModelParameters;
import org.librairy.service.learner.facade.rest.model.Result;
import org.librairy.service.modeler.controllers.RestDimensionsController;
import org.librairy.service.modeler.facade.model.ModelerService;
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

@RestController
@RequestMapping("/dimensions")
@Api(tags="/dimensions", description = "topics management")
public class LearnerRestDimensionsController extends RestDimensionsController {

    private static final Logger LOG = LoggerFactory.getLogger(LearnerRestDimensionsController.class);

    @Autowired
    ModelerService modelerService;

    @Autowired
    LearnerService learnerService;

    @PostConstruct
    public void setup(){

    }

    @PreDestroy
    public void destroy(){

    }


    @ApiOperation(value = "discover", nickname = "postTopics", response=String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Accepted", response = String.class),
    })
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<Result> train(@RequestBody ModelParameters request)  {
        try {
            return new ResponseEntity(new Result(learnerService.train(request.getParameters())), HttpStatus.ACCEPTED);
        } catch (AvroRemoteException e) {
            return new ResponseEntity(new Result("internal service seems down"),HttpStatus.FAILED_DEPENDENCY);
        } catch (Exception e) {
            LOG.error("IO Error", e);
            return new ResponseEntity(new Result("IO error"),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
