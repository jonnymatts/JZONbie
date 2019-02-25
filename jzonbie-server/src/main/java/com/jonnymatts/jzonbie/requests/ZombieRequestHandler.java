package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.Request;
import com.jonnymatts.jzonbie.Response;
import com.jonnymatts.jzonbie.priming.CallHistory;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.priming.PrimingContext;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.responses.CurrentPrimingFileResponseFactory;
import com.jonnymatts.jzonbie.responses.CurrentPrimingFileResponseFactory.FileResponse;
import com.jonnymatts.jzonbie.util.Deserializer;
import com.jonnymatts.jzonbie.verification.CountResult;

import java.util.List;

import static com.jonnymatts.jzonbie.responses.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.OK_200;

public class ZombieRequestHandler implements RequestHandler {

    private final PrimingContext primingContext;
    private final CallHistory callHistory;
    private final List<AppRequest> failedRequests;
    private final Deserializer deserializer;
    private final String zombieHeaderName;
    private final CurrentPrimingFileResponseFactory fileResponseFactory;
    private final PrimedMappingUploader primedMappingUploader;

    public ZombieRequestHandler(String zombieHeaderName,
                                PrimingContext primingContext,
                                CallHistory callHistory,
                                List<AppRequest> failedRequests,
                                Deserializer deserializer,
                                CurrentPrimingFileResponseFactory fileResponseFactory,
                                PrimedMappingUploader primedMappingUploader) {
        this.zombieHeaderName = zombieHeaderName;
        this.primingContext = primingContext;
        this.callHistory = callHistory;
        this.failedRequests = failedRequests;
        this.deserializer = deserializer;
        this.fileResponseFactory = fileResponseFactory;
        this.primedMappingUploader = primedMappingUploader;
    }

    @Override
    public Response handle(Request request) {
        final String zombieHeaderValue = request.getHeaders().get(zombieHeaderName);

        switch(zombieHeaderValue) {
            case "priming":
                return handlePrimingRequest(request);
            case "priming-default":
                return handleDefaultPrimingRequest(request);
            case "priming-file":
                return handleFilePrimingRequest(request);
            case "count":
                return handleCountRequest(request);
            case "current":
                return handleCurrentPrimingRequest();
            case "current-file":
                return handleCurrentPrimingFileRequest();
            case "history":
                return handleHistoryRequest();
            case "failed":
                return handleFailedRequest();
            case "reset":
                return handleResetRequest();
            default:
                throw new RuntimeException(format("Unknown zombie method: %s", zombieHeaderValue));
        }
    }

    private ZombieResponse handlePrimingRequest(Request request) {
        final ZombiePriming zombiePriming = getZombiePriming(request);

        primingContext.add(zombiePriming.getRequest(), zombiePriming.getResponse());

        return new ZombieResponse(CREATED_201, zombiePriming);
    }

    private ZombieResponse handleDefaultPrimingRequest(Request request) {
        final ZombiePriming zombiePriming = getZombiePriming(request);

        primingContext.addDefault(zombiePriming.getRequest(), staticDefault(zombiePriming.getResponse()));

        return new ZombieResponse(CREATED_201, zombiePriming);
    }

    private ZombieResponse handleFilePrimingRequest(Request request) {
        final List<PrimedMapping> primedMappings = deserializer.deserializeCollection(request.getPrimingFileContent(), PrimedMapping.class);

        primedMappingUploader.upload(primedMappings);

        return new ZombieResponse(CREATED_201, primedMappings);
    }

    private ZombieResponse handleCurrentPrimingRequest() {
        return new ZombieResponse(OK_200, primingContext.getCurrentPriming());
    }

    private FileResponse handleCurrentPrimingFileRequest() {
        return fileResponseFactory.create(primingContext.getCurrentPriming());
    }

    private ZombieResponse handleHistoryRequest() {
        return new ZombieResponse(OK_200, callHistory);
    }

    private ZombieResponse handleFailedRequest() {
        return new ZombieResponse(OK_200, failedRequests);
    }

    private ZombieResponse handleCountRequest(Request request) {
        final AppRequest appRequest = deserializer.deserialize(request, AppRequest.class);
        final int count = callHistory.count(appRequest);
        return new ZombieResponse(OK_200, new CountResult(count));
    }

    private ZombieResponse handleResetRequest() {
        primingContext.reset();
        callHistory.clear();
        failedRequests.clear();
        return new ZombieResponse(OK_200, singletonMap("message", "Zombie Reset"));
    }

    private ZombiePriming getZombiePriming(Request request) {
        final ZombiePriming zombiePriming = deserializer.deserialize(request, ZombiePriming.class);
        final AppRequest zombieRequest = zombiePriming.getRequest();

        if (zombieRequest.getMethod() == null) {
            throw new IllegalArgumentException("Method cannot be null");
        }

        if (zombieRequest.getPath() == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        return zombiePriming;
    }
}