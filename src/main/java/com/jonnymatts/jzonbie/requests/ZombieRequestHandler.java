package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.PrimedMappingFactory;
import com.jonnymatts.jzonbie.model.PrimingContext;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.response.Response;
import com.jonnymatts.jzonbie.util.Deserializer;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.OK_200;

public class ZombieRequestHandler implements RequestHandler {

    public static final Map<String, String> JSON_HEADERS_MAP = singletonMap("Content-Type", "application/json");

    private final PrimingContext primingContext;
    private final List<ZombiePriming> callHistory;
    private final Deserializer deserializer;
    private final PrimedMappingFactory primedMappingFactory;

    public ZombieRequestHandler(PrimingContext primingContext,
                                List<ZombiePriming> callHistory,
                                Deserializer deserializer,
                                PrimedMappingFactory primedMappingFactory) {
        this.primingContext = primingContext;
        this.callHistory = callHistory;
        this.deserializer = deserializer;
        this.primedMappingFactory = primedMappingFactory;
    }

    @Override
    public Response handle(Request request) throws JsonProcessingException {
        final String zombieHeaderValue = request.getHeaders().get("zombie");

        switch(zombieHeaderValue) {
            case "priming":
                return handlePrimingRequest(request);
            case "priming-default":
                return handleDefaultPrimingRequest(request);
            case "list":
                return handleListRequest();
            case "history":
                return handleHistoryRequest();
            case "reset":
                return handleResetRequest();
            default:
                throw new RuntimeException(format("Unknown zombie method: %s", zombieHeaderValue));
        }
    }

    private ZombieResponse handlePrimingRequest(Request request) throws JsonProcessingException {
        final ZombiePriming zombiePriming = getZombiePriming(request);

        primingContext.add(zombiePriming.getAppRequest(), zombiePriming.getAppResponse());

        return new ZombieResponse(CREATED_201, JSON_HEADERS_MAP, zombiePriming);
    }

    private ZombieResponse handleDefaultPrimingRequest(Request request) throws JsonProcessingException {
        final ZombiePriming zombiePriming = getZombiePriming(request);

        primingContext.addDefault(zombiePriming.getAppRequest(), zombiePriming.getAppResponse());

        return new ZombieResponse(CREATED_201, JSON_HEADERS_MAP, zombiePriming);
    }

    private ZombieResponse handleListRequest() throws JsonProcessingException {
        return new ZombieResponse(OK_200, JSON_HEADERS_MAP, primingContext.getCurrentPriming());
    }

    private ZombieResponse handleHistoryRequest() throws JsonProcessingException {
        return new ZombieResponse(OK_200, JSON_HEADERS_MAP, callHistory);
    }

    private ZombieResponse handleResetRequest() {
        primingContext.clear();
        callHistory.clear();
        return new ZombieResponse(OK_200, emptyMap(), "Zombie Reset");
    }

    private ZombiePriming getZombiePriming(Request request) {
        final ZombiePriming zombiePriming = deserializer.deserialize(request, ZombiePriming.class);
        final AppRequest zombieRequest = zombiePriming.getAppRequest();

        if(zombieRequest.getMethod() == null) {
            throw new IllegalArgumentException("Method cannot be null");
        }

        if(zombieRequest.getPath() == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        return zombiePriming;
    }

    private class ZombieResponse implements Response {

        private final int statusCode;
        private final Map<String, String> headers;
        private final Object body;

        public ZombieResponse(int statusCode, Map<String, String> headers, Object body) {
            this.statusCode = statusCode;
            this.headers = headers;
            this.body = body;
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public Map<String, String> getHeaders() {
            return headers;
        }

        @Override
        public Object getBody() {
            return body;
        }
    }
}