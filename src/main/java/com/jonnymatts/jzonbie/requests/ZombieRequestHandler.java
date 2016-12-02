package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Multimap;
import com.jonnymatts.jzonbie.util.Deserializer;
import com.jonnymatts.jzonbie.model.PrimedRequest;
import com.jonnymatts.jzonbie.model.PrimedMappingFactory;
import com.jonnymatts.jzonbie.model.PrimedResponse;
import com.jonnymatts.jzonbie.model.JZONbieRequest;
import spark.Request;
import spark.Response;

import java.util.List;

import static java.lang.String.format;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.OK_200;

public class ZombieRequestHandler implements RequestHandler {

    private final Multimap<PrimedRequest, PrimedResponse> primingContext;
    private final List<JZONbieRequest> callHistory;
    private final Deserializer deserializer;
    private final ObjectMapper objectMapper;
    private final PrimedMappingFactory primedMappingFactory;

    public ZombieRequestHandler(Multimap<PrimedRequest, PrimedResponse> primingContext,
                                List<JZONbieRequest> callHistory,
                                Deserializer deserializer,
                                ObjectMapper objectMapper,
                                PrimedMappingFactory primedMappingFactory) {
        this.primingContext = primingContext;
        this.callHistory = callHistory;
        this.deserializer = deserializer;
        this.objectMapper = objectMapper;
        this.primedMappingFactory = primedMappingFactory;
    }

    @Override
    public String handle(Request request, Response response) throws JsonProcessingException {
        final String zombieHeaderValue = request.headers("zombie");

        switch(zombieHeaderValue) {
            case "priming":
                return handlePrimingRequest(request, response);
            case "list":
                return handleListRequest(response);
            case "history":
                return handleHistoryRequest(response);
            case "reset":
                return handleResetRequest(response);
            default:
                throw new RuntimeException(format("Unknown zombie method: %s", zombieHeaderValue));
        }
    }

    private String handleHistoryRequest(Response response) throws JsonProcessingException {
        response.status(OK_200);
        return objectMapper.writeValueAsString(callHistory);
    }

    private String handleResetRequest(Response response) {
        response.status(OK_200);
        primingContext.clear();
        callHistory.clear();
        return "Zombie Reset";
    }

    private String handlePrimingRequest(Request request, Response response) throws JsonProcessingException {
        final JZONbieRequest JZONbieRequest = deserializer.deserialize(request, JZONbieRequest.class);
        final PrimedRequest primedRequest = JZONbieRequest.getPrimedRequest();
        final PrimedResponse primedResponse = JZONbieRequest.getPrimedResponse();

        if(primedRequest.getMethod() == null) {
            primedRequest.setMethod(request.requestMethod());
        }

        if(primedRequest.getPath() == null) {
            primedRequest.setPath(request.pathInfo());
        }

        primingContext.put(primedRequest, primedResponse);

        response.status(CREATED_201);

        return objectMapper.writeValueAsString(JZONbieRequest);
    }

    private String handleListRequest(Response response) throws JsonProcessingException {
        response.status(OK_200);
        response.header("Content-Type", "application/json");
        return objectMapper.writeValueAsString(primedMappingFactory.create(primingContext));
    }
}
