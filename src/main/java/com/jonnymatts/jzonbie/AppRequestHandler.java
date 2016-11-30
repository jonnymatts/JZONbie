package com.jonnymatts.jzonbie;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Multimap;
import spark.Request;
import spark.Response;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class AppRequestHandler implements RequestHandler {

    private final Multimap<PrimedRequest, PrimedResponse> primingContext;
    private final PrimedRequestFactory primedRequestFactory;
    private final ObjectMapper objectMapper;

    public AppRequestHandler(Multimap<PrimedRequest, PrimedResponse> primingContext,
                             PrimedRequestFactory primedRequestFactory,
                             ObjectMapper objectMapper) {
        this.primingContext = primingContext;
        this.primedRequestFactory = primedRequestFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public String handle(Request request, Response response) throws JsonProcessingException {
        final PrimedRequest primedRequest = primedRequestFactory.create(request);

        final Collection<PrimedResponse> primedResponses = primingContext.get(primedRequest);
        final Optional<PrimedResponse> primedResponseOpt = primedResponses.stream().findFirst();

        if(!primedResponseOpt.isPresent()) {
            return errorResponse(response);
        }

        final PrimedResponse primedResponse = primedResponseOpt.get();

        primeResponse(response, primedResponse);

        primingContext.remove(primedRequest, primedResponse);

        return objectMapper.writeValueAsString(primedResponse.getBody());
    }

    private void primeResponse(Response response, PrimedResponse r) throws JsonProcessingException {
        response.status(r.getStatusCode());

        final Map<String, String> headers = r.getHeaders();

        if(headers != null) headers.entrySet().forEach(entry -> response.header(entry.getKey(), entry.getValue()));
    }

    private String errorResponse(Response response) {
        return null;
    }
}
