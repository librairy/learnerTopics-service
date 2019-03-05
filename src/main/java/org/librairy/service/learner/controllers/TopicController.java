package org.librairy.service.learner.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.librairy.service.learner.builders.DateBuilder;
import org.librairy.service.learner.facade.rest.model.Result;
import org.librairy.service.learner.facade.rest.model.TopicsRequest;
import org.librairy.service.learner.model.Task;
import org.librairy.service.learner.service.QueueService;
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
@RequestMapping("/topics")
@Api(tags="/topics", description = "topics management")
public class TopicController {

    private static final Logger LOG = LoggerFactory.getLogger(TopicController.class);

    @Autowired
    QueueService queueService;

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
    public ResponseEntity<Result> create(@RequestBody TopicsRequest request)  {
        String date = DateBuilder.now();
        try {
            if (!request.isValid()) return new ResponseEntity(new Result(date, "REJECTED", "Bad Request"),HttpStatus.BAD_REQUEST);

            // train a new model from datasource
            queueService.add(new Task(request));
            return new ResponseEntity(new Result(date,"QUEUED","Task created"), HttpStatus.ACCEPTED);
        } catch (Exception e) {
            LOG.error("IO Error", e);
            return new ResponseEntity(new Result(date, "REJECTED", "IO error"),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
