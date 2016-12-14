package com.jonnymatts.jzonbie.requests;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Multimap;
import com.jonnymatts.jzonbie.model.ZombieRequest;
import com.jonnymatts.jzonbie.model.PrimedRequestFactory;
import com.jonnymatts.jzonbie.model.ZombieResponse;
import com.jonnymatts.jzonbie.model.PrimingRequest;
import com.jonnymatts.jzonbie.repsonse.PrimingNotFoundErrorResponse;
import spark.Request;
import spark.Response;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;

public class AppRequestHandler implements RequestHandler {

    private final Multimap<ZombieRequest, ZombieResponse> primingContext;
    private final List<PrimingRequest> callHistory;
    private final PrimedRequestFactory primedRequestFactory;

    public AppRequestHandler(Multimap<ZombieRequest, ZombieResponse> primingContext,
                             List<PrimingRequest> callHistory,
                             PrimedRequestFactory primedRequestFactory) {
        this.primingContext = primingContext;
        this.callHistory = callHistory;
        this.primedRequestFactory = primedRequestFactory;
    }

    @Override
    public Object handle(Request request, Response response) throws JsonProcessingException {
        final ZombieRequest zombieRequest = primedRequestFactory.create(request);

        final Collection<ZombieResponse> zombieResponses = primingContext.get(zombieRequest);
        final Optional<ZombieResponse> primedResponseOpt = zombieResponses.stream().findFirst();

        if(!primedResponseOpt.isPresent()) {
            throw new PrimingNotFoundException(zombieRequest);
        }

        final ZombieResponse zombieResponse = primedResponseOpt.get();

        primeResponse(response, zombieResponse);

        primingContext.remove(zombieRequest, zombieResponse);

        callHistory.add(new PrimingRequest(zombieRequest, zombieResponse));

        return zombieResponse.getBody();
    }

    private void primeResponse(Response response, ZombieResponse r) throws JsonProcessingException {
        response.status(r.getStatusCode());

        final Map<String, String> headers = r.getHeaders();

        if(headers != null) headers.entrySet().forEach(entry -> response.header(entry.getKey(), entry.getValue()));
    }
}
