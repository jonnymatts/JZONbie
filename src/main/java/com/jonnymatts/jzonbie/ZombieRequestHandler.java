package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Multimap;
import spark.Request;
import spark.Response;

import java.util.List;

import static java.lang.String.format;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.OK_200;

public class ZombieRequestHandler implements RequestHandler {

    private final Multimap<PrimedRequest, PrimedResponse> primingContext;
    private final JsonDeserializer jsonDeserializer;
    private final ObjectMapper objectMapper;
    private final PrimedRequestsFactory primedRequestsFactory;

    public ZombieRequestHandler(Multimap<PrimedRequest, PrimedResponse> primingContext,
                                JsonDeserializer jsonDeserializer,
                                ObjectMapper objectMapper,
                                PrimedRequestsFactory primedRequestsFactory) {
        this.primingContext = primingContext;
        this.jsonDeserializer = jsonDeserializer;
        this.objectMapper = objectMapper;
        this.primedRequestsFactory = primedRequestsFactory;
    }

    @Override
    public String handle(Request request, Response response) throws JsonProcessingException {
        final String zombieHeaderValue = request.headers("zombie");

        switch(zombieHeaderValue) {
            case "priming":
                return handlePrimingRequest(request, response);
            case "list":
                return handleListRequest(response);
            default:
                throw new RuntimeException(format("Unknown zombie method: %s", zombieHeaderValue));
        }
    }

    private String handlePrimingRequest(Request request, Response response) throws JsonProcessingException {
        final PrimingRequest primingRequest = jsonDeserializer.deserialize(request, PrimingRequest.class);
        final PrimedRequest primedRequest = primingRequest.getPrimedRequest();
        final PrimedResponse primedResponse = primingRequest.getPrimedResponse();

        if(primedRequest.getMethod() == null) {
            primedRequest.setMethod(request.requestMethod());
        }

        if(primedRequest.getPath() == null) {
            primedRequest.setPath(request.pathInfo());
        }

        primingContext.put(primedRequest, primedResponse);

        response.status(CREATED_201);

        return objectMapper.writeValueAsString(primingRequest);
    }

    private String handleListRequest(Response response) throws JsonProcessingException {
        response.status(OK_200);
        response.header("Content-Type", "application/json");
        return objectMapper.writeValueAsString(primedRequestsFactory.create(primingContext));
    }
}
