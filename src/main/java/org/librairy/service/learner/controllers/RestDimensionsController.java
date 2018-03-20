package org.librairy.service.learner.controllers;

import io.swagger.annotations.*;
import org.apache.avro.AvroRemoteException;
import org.librairy.service.learner.facade.model.LearnerService;
import org.librairy.service.learner.facade.rest.model.ModelParameters;
import org.librairy.service.learner.facade.rest.model.Result;
import org.librairy.service.modeler.facade.model.ModelerService;
import org.librairy.service.modeler.facade.rest.model.Dimension;
import org.librairy.service.modeler.facade.rest.model.DimensionList;
import org.librairy.service.modeler.facade.rest.model.Element;
import org.librairy.service.modeler.facade.rest.model.ElementList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dimensions")
@Api(tags="/dimensions", description = "topics management")
public class RestDimensionsController {

    private static final Logger LOG = LoggerFactory.getLogger(RestDimensionsController.class);

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

    @ApiOperation(value = "list of", nickname = "getTopics", response=DimensionList.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = DimensionList.class),
    })
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<DimensionList> get()  {
        try {
            return new ResponseEntity(new DimensionList(modelerService.dimensions().stream().map(t -> new Dimension(t)).collect(Collectors.toList())), HttpStatus.OK);
        } catch (AvroRemoteException e) {
            return new ResponseEntity("internal service seems down",HttpStatus.FAILED_DEPENDENCY);
        } catch (Exception e) {
            LOG.error("IO Error", e);
            return new ResponseEntity("IO error",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "top words of a given topic", nickname = "getWords", response=ElementList.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = ElementList.class),
    })
    @RequestMapping(value = "/{id:.+}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<ElementList> get(
            @ApiParam (value = "id", required = true) @PathVariable Integer id,
            @RequestParam Integer maxWords)  {
        try {
            return new ResponseEntity(new ElementList(modelerService.elements(id,maxWords).stream().map(w -> new Element(w)).collect(Collectors.toList())), HttpStatus.OK);
        } catch (AvroRemoteException e) {
            return new ResponseEntity("internal service seems down",HttpStatus.FAILED_DEPENDENCY);
        } catch (Exception e) {
            LOG.error("IO Error", e);
            return new ResponseEntity("IO error",HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
