package com.jonnymatts.jzonbie;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Multimap;
import spark.Request;
import spark.Response;

import java.util.Collection;
import java.util.Optional;

public class AppRequestHandler implements RequestHandler {

    private final Multimap<PrimingKey, PrimedResponse> primingContext;
    private final PrimingKeyFactory primingKeyFactory;
    private final ObjectMapper objectMapper;

    public AppRequestHandler(Multimap<PrimingKey, PrimedResponse> primingContext,
                             PrimingKeyFactory primingKeyFactory,
                             ObjectMapper objectMapper) {
        this.primingContext = primingContext;
        this.primingKeyFactory = primingKeyFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public String handle(Request request, Response response) throws JsonProcessingException {
        final PrimingKey primingKey = primingKeyFactory.create(request);

        final Collection<PrimedResponse> primedResponses = primingContext.get(primingKey);
        final Optional<PrimedResponse> primedResponseOpt = primedResponses.stream().findFirst();

        if(!primedResponseOpt.isPresent()) {
            return errorResponse(response);
        }

        final PrimedResponse primedResponse = primedResponseOpt.get();

        primeResponse(response, primedResponse);

        primingContext.remove(primingKey, primedResponse);

        return objectMapper.writeValueAsString(primedResponse.getBody());
    }

    private void primeResponse(Response response, PrimedResponse r) throws JsonProcessingException {
        response.status(r.getStatusCode());
        r.getHeaders().entrySet().forEach(entry -> response.header(entry.getKey(), entry.getValue()));
    }

    private String errorResponse(Response response) {
        return null;
    }
}
