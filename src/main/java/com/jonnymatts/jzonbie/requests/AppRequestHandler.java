package com.jonnymatts.jzonbie.requests;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Multimap;
import com.jonnymatts.jzonbie.model.PrimedRequest;
import com.jonnymatts.jzonbie.model.PrimedRequestFactory;
import com.jonnymatts.jzonbie.model.PrimedResponse;
import com.jonnymatts.jzonbie.model.JZONbieRequest;
import spark.Request;
import spark.Response;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AppRequestHandler implements RequestHandler {

    private final Multimap<PrimedRequest, PrimedResponse> primingContext;
    private final List<JZONbieRequest> callHistory;
    private final PrimedRequestFactory primedRequestFactory;

    public AppRequestHandler(Multimap<PrimedRequest, PrimedResponse> primingContext,
                             List<JZONbieRequest> callHistory,
                             PrimedRequestFactory primedRequestFactory) {
        this.primingContext = primingContext;
        this.callHistory = callHistory;
        this.primedRequestFactory = primedRequestFactory;
    }

    @Override
    public Object handle(Request request, Response response) throws JsonProcessingException {
        final PrimedRequest primedRequest = primedRequestFactory.create(request);

        final Collection<PrimedResponse> primedResponses = primingContext.get(primedRequest);
        final Optional<PrimedResponse> primedResponseOpt = primedResponses.stream().findFirst();

        if(!primedResponseOpt.isPresent()) {
            return errorResponse(response);
        }

        final PrimedResponse primedResponse = primedResponseOpt.get();

        primeResponse(response, primedResponse);

        primingContext.remove(primedRequest, primedResponse);

        callHistory.add(new JZONbieRequest(primedRequest, primedResponse));

        return primedResponse.getBody();
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
