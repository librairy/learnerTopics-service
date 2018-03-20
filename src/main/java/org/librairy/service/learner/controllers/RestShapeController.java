package org.librairy.service.learner.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.avro.AvroRemoteException;
import org.librairy.service.modeler.facade.model.ModelerService;
import org.librairy.service.modeler.facade.rest.model.Shape;
import org.librairy.service.modeler.facade.rest.model.ShapeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@RestController
@RequestMapping("/shape")
@Api(tags="/shape", description = "vector management")
public class RestShapeController {

    private static final Logger LOG = LoggerFactory.getLogger(RestShapeController.class);

    @Autowired
    ModelerService service;

    @PostConstruct
    public void setup(){

    }

    @PreDestroy
    public void destroy(){

    }

    @ApiOperation(value = "obtain a probabilistic vector", nickname = "postShape", response=Shape.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Shape.class),
    })
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public Shape shape(@RequestBody ShapeRequest request)  {
        try {
            return new Shape(service.shape(request.getText()));
        } catch (AvroRemoteException e) {
            throw new RuntimeException(e);
        }
    }

}
