package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Multimap;
import com.jonnymatts.jzonbie.model.PrimedMappingFactory;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.model.ZombieRequest;
import com.jonnymatts.jzonbie.model.ZombieResponse;
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
    public Response handle(Request request) throws JsonProcessingException {
        final String zombieHeaderValue = request.getHeaders().get("zombie");

        switch(zombieHeaderValue) {
            case "priming":
                return handlePrimingRequest(request);
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

    private ZResponse handlePrimingRequest(Request request) throws JsonProcessingException {
        final ZombiePriming zombiePriming = deserializer.deserialize(request, ZombiePriming.class);
        final ZombieRequest zombieRequest = zombiePriming.getZombieRequest();
        final ZombieResponse zombieResponse = zombiePriming.getZombieResponse();

        if(zombieRequest.getMethod() == null) {
            zombieRequest.setMethod(request.getMethod());
        }

        if(zombieRequest.getPath() == null) {
            zombieRequest.setPath(request.getPath());
        }

        primingContext.put(zombieRequest, zombieResponse);

        return new ZResponse(CREATED_201, JSON_HEADERS_MAP, zombiePriming);
    }

    private ZResponse handleListRequest() throws JsonProcessingException {
        return new ZResponse(OK_200, JSON_HEADERS_MAP, primedMappingFactory.create(primingContext));
    }

    private ZResponse handleHistoryRequest() throws JsonProcessingException {
        return new ZResponse(OK_200, JSON_HEADERS_MAP, callHistory);
    }

    private ZResponse handleResetRequest() {
        primingContext.clear();
        callHistory.clear();
        return new ZResponse(OK_200, emptyMap(), "Zombie Reset");
    }

    private class ZResponse implements Response {

        private final int statusCode;
        private final Map<String, String> headers;
        private final Object body;

        public ZResponse(int statusCode, Map<String, String> headers, Object body) {
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