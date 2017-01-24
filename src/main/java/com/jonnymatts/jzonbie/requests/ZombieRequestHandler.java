package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Multimap;
import com.jonnymatts.jzonbie.model.*;
import com.jonnymatts.jzonbie.util.Deserializer;
import spark.Response;

import java.util.List;

import static java.lang.String.format;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.OK_200;

public class ZombieRequestHandler implements RequestHandler {

    private final Multimap<ZombieRequest, ZombieResponse> primingContext;
    private final List<ZombiePriming> callHistory;
    private final Deserializer deserializer;
    private final PrimedMappingFactory primedMappingFactory;

    public ZombieRequestHandler(Multimap<ZombieRequest, ZombieResponse> primingContext,
                                List<ZombiePriming> callHistory,
                                Deserializer deserializer,
                                PrimedMappingFactory primedMappingFactory) {
        this.primingContext = primingContext;
        this.callHistory = callHistory;
        this.deserializer = deserializer;
        this.primedMappingFactory = primedMappingFactory;
    }

    @Override
    public Object handle(Request request, Response response) throws JsonProcessingException {
        final String zombieHeaderValue = request.getHeaders().get("zombie");

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

    private ZombiePriming handlePrimingRequest(Request request, Response response) throws JsonProcessingException {
        final ZombiePriming ZombiePriming = deserializer.deserialize(request, ZombiePriming.class);
        final ZombieRequest zombieRequest = ZombiePriming.getZombieRequest();
        final ZombieResponse zombieResponse = ZombiePriming.getZombieResponse();

        if(zombieRequest.getMethod() == null) {
            zombieRequest.setMethod(request.getMethod());
        }

        if(zombieRequest.getPath() == null) {
            zombieRequest.setPath(request.getPath());
        }

        primingContext.put(zombieRequest, zombieResponse);

        response.status(CREATED_201);
        response.header("Content-Type", "application/json");

        return ZombiePriming;
    }

    private List<PrimedMapping> handleListRequest(Response response) throws JsonProcessingException {
        response.status(OK_200);
        response.header("Content-Type", "application/json");
        return primedMappingFactory.create(primingContext);
    }

    private List<ZombiePriming> handleHistoryRequest(Response response) throws JsonProcessingException {
        response.status(OK_200);
        response.header("Content-Type", "application/json");
        return callHistory;
    }

    private String handleResetRequest(Response response) {
        response.status(OK_200);
        primingContext.clear();
        callHistory.clear();
        return "Zombie Reset";
    }
}