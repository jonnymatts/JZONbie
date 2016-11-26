package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Multimap;
import spark.Request;
import spark.Response;

import static java.lang.String.format;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;

public class ZombieRequestHandler implements RequestHandler {

    private final Multimap<PrimingKey, PrimedResponse> primingContext;
    private final JsonDeserializer jsonDeserializer;
    private final ObjectMapper objectMapper;

    public ZombieRequestHandler(Multimap<PrimingKey, PrimedResponse> primingContext,
                                JsonDeserializer jsonDeserializer, ObjectMapper objectMapper) {
        this.primingContext = primingContext;
        this.jsonDeserializer = jsonDeserializer;
        this.objectMapper = objectMapper;
    }

    @Override
    public String handle(Request request, Response response) throws JsonProcessingException {
        final String zombieHeaderValue = request.headers("zombie");

        switch(zombieHeaderValue) {
            case "priming":
                return handlePrimingRequest(request, response);
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

        final PrimingKey primingKey = new PrimingKey(request.pathInfo(), primedRequest);

        primingContext.put(primingKey, primedResponse);

        response.status(CREATED_201);

        return objectMapper.writeValueAsString(primingRequest);
    }
}
