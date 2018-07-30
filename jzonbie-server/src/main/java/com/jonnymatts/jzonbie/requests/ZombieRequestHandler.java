package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jonnymatts.jzonbie.JzonbieOptions;
import com.jonnymatts.jzonbie.model.*;
import com.jonnymatts.jzonbie.response.CurrentPrimingFileResponseFactory;
import com.jonnymatts.jzonbie.response.CurrentPrimingFileResponseFactory.FileResponse;
import com.jonnymatts.jzonbie.response.Response;
import com.jonnymatts.jzonbie.util.Deserializer;
import com.jonnymatts.jzonbie.verification.CountResult;

import java.util.List;
import java.util.Map;

import static com.jonnymatts.jzonbie.model.Cloner.createTemplatedResponse;
import static com.jonnymatts.jzonbie.response.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.OK_200;

public class ZombieRequestHandler implements RequestHandler {

    public static final Map<String, String> JSON_HEADERS_MAP = singletonMap("Content-Type", "application/json");

    private final JzonbieOptions options;
    private final PrimingContext primingContext;
    private final CallHistory callHistory;
    private final List<AppRequest> failedRequests;
    private final Deserializer deserializer;
    private final String zombieHeaderName;
    private final CurrentPrimingFileResponseFactory fileResponseFactory;
    private final PrimedMappingUploader primedMappingUploader;

    public ZombieRequestHandler(JzonbieOptions options,
                                PrimingContext primingContext,
                                CallHistory callHistory,
                                List<AppRequest> failedRequests,
                                Deserializer deserializer,
                                CurrentPrimingFileResponseFactory fileResponseFactory,
                                PrimedMappingUploader primedMappingUploader) {
        this.options = options;
        this.primingContext = primingContext;
        this.callHistory = callHistory;
        this.failedRequests = failedRequests;
        this.deserializer = deserializer;
        this.zombieHeaderName = options.getZombieHeaderName();
        this.fileResponseFactory = fileResponseFactory;
        this.primedMappingUploader = primedMappingUploader;
    }

    @Override
    public Response handle(Request request) throws JsonProcessingException {
        final String zombieHeaderValue = request.getHeaders().get(zombieHeaderName);

        switch(zombieHeaderValue) {
            case "priming":
                return handlePrimingRequest(request);
            case "priming-template":
                return handleTemplatedPrimingRequest(request);
            case "priming-default":
                return handleDefaultPrimingRequest(request);
            case "priming-default-template":
                return handleTemplatedDefaultPrimingRequest(request);
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

    private ZombieResponse handlePrimingRequest(Request request) throws JsonProcessingException {
        final ZombiePriming zombiePriming = getZombiePriming(request);

        primingContext.add(zombiePriming.getAppRequest(), zombiePriming.getAppResponse());

        return new ZombieResponse(CREATED_201, JSON_HEADERS_MAP, zombiePriming);
    }

    private ZombieResponse handleTemplatedPrimingRequest(Request request) throws JsonProcessingException {
        final ZombiePriming zombiePriming = getZombiePriming(request);

        final TemplatedAppResponse templatedAppResponse = createTemplatedResponse(zombiePriming.getAppResponse());
        zombiePriming.setAppResponse(templatedAppResponse);

        primingContext.add(zombiePriming.getAppRequest(), zombiePriming.getAppResponse());

        return new ZombieResponse(CREATED_201, JSON_HEADERS_MAP, zombiePriming);
    }

    private ZombieResponse handleDefaultPrimingRequest(Request request) throws JsonProcessingException {
        final ZombiePriming zombiePriming = getZombiePriming(request);

        primingContext.addDefault(zombiePriming.getAppRequest(), staticDefault(zombiePriming.getAppResponse()));

        return new ZombieResponse(CREATED_201, JSON_HEADERS_MAP, zombiePriming);
    }

    private ZombieResponse handleTemplatedDefaultPrimingRequest(Request request) throws JsonProcessingException {
        final ZombiePriming zombiePriming = getZombiePriming(request);

        final TemplatedAppResponse templatedAppResponse = createTemplatedResponse(zombiePriming.getAppResponse());
        zombiePriming.setAppResponse(templatedAppResponse);

        primingContext.addDefault(zombiePriming.getAppRequest(), staticDefault(zombiePriming.getAppResponse()));

        return new ZombieResponse(CREATED_201, JSON_HEADERS_MAP, zombiePriming);
    }

    private ZombieResponse handleFilePrimingRequest(Request request) throws JsonProcessingException {
        final List<PrimedMapping> primedMappings = deserializer.deserializeCollection(request.getPrimingFileContent(), PrimedMapping.class);

        primedMappingUploader.upload(primedMappings);

        return new ZombieResponse(CREATED_201, JSON_HEADERS_MAP, primedMappings);
    }

    private ZombieResponse handleCurrentPrimingRequest() throws JsonProcessingException {
        return new ZombieResponse(OK_200, JSON_HEADERS_MAP, primingContext.getCurrentPriming());
    }

    private FileResponse handleCurrentPrimingFileRequest() throws JsonProcessingException {
        return fileResponseFactory.create(primingContext.getCurrentPriming());
    }

    private ZombieResponse handleHistoryRequest() throws JsonProcessingException {
        return new ZombieResponse(OK_200, JSON_HEADERS_MAP, callHistory);
    }

    private ZombieResponse handleFailedRequest() throws JsonProcessingException {
        return new ZombieResponse(OK_200, JSON_HEADERS_MAP, failedRequests);
    }

    private ZombieResponse handleCountRequest(Request request) throws JsonProcessingException {
        final AppRequest appRequest = deserializer.deserialize(request, AppRequest.class);
        final int count = callHistory.count(appRequest);
        return new ZombieResponse(OK_200, JSON_HEADERS_MAP, new CountResult(count));
    }

    private ZombieResponse handleResetRequest() {
        primingContext.clear();
        callHistory.clear();
        failedRequests.clear();
        return new ZombieResponse(OK_200, JSON_HEADERS_MAP, singletonMap("message", "Zombie Reset"));
    }

    private ZombiePriming getZombiePriming(Request request) {
        final ZombiePriming zombiePriming = deserializer.deserialize(request, ZombiePriming.class);
        final AppRequest zombieRequest = zombiePriming.getAppRequest();

        if (zombieRequest.getMethod() == null) {
            throw new IllegalArgumentException("Method cannot be null");
        }

        if (zombieRequest.getPath() == null) {
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

        @Override
        public boolean isFileResponse() {
            return false;
        }
    }
}