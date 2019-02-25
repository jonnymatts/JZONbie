package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.jonnymatts.jzonbie.jetty.JzonbieJettyServer;
import com.jonnymatts.jzonbie.pippo.PippoApplication;
import com.jonnymatts.jzonbie.priming.*;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.requests.AppRequestHandler;
import com.jonnymatts.jzonbie.requests.PrimedMappingUploader;
import com.jonnymatts.jzonbie.requests.ZombieRequestHandler;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.CurrentPrimingFileResponseFactory;
import com.jonnymatts.jzonbie.responses.DefaultAppResponse;
import com.jonnymatts.jzonbie.responses.DefaultAppResponse.StaticDefaultAppResponse;
import com.jonnymatts.jzonbie.util.Deserializer;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
import com.jonnymatts.jzonbie.verification.VerificationException;
import ro.pippo.core.Pippo;
import ro.pippo.core.util.IoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.jonnymatts.jzonbie.JzonbieOptions.options;

public class Jzonbie implements JzonbieClient {

    private final PrimingContext primingContext;
    private final CallHistory callHistory = new CallHistory();
    private final List<AppRequest> failedRequests = new ArrayList<>();
    private final int port;
    private final Pippo pippo;
    private Deserializer deserializer;
    private ObjectMapper objectMapper;
    private PrimedMappingUploader primedMappingUploader;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Duration> waitAfterStop;

    public Jzonbie() {
        this(options());
    }

    public Jzonbie(JzonbieOptions options) {
        primingContext = new PrimingContext(options.getDefaultPriming());
        waitAfterStop = options.getWaitAfterStopping();
        objectMapper = options.getObjectMapper();
        deserializer = new Deserializer(objectMapper);
        final AppRequestFactory appRequestFactory = new AppRequestFactory(deserializer);
        final CurrentPrimingFileResponseFactory fileResponseFactory = new CurrentPrimingFileResponseFactory(objectMapper);
        primedMappingUploader = new PrimedMappingUploader(primingContext);
        final AppRequestHandler appRequestHandler = new AppRequestHandler(primingContext, callHistory, failedRequests, appRequestFactory);
        final ZombieRequestHandler zombieRequestHandler = new ZombieRequestHandler(options.getZombieHeaderName(), primingContext, callHistory, failedRequests, deserializer, fileResponseFactory, primedMappingUploader);

        options.getRoutes().forEach(route -> {
            route.setJzonbieClient(this);
            route.setDeserializer(deserializer);
        });

        final Handlebars handlebars = new Handlebars();
        pippo = new Pippo(new PippoApplication(options, appRequestHandler, zombieRequestHandler, options.getObjectMapper(), options.getRoutes(), handlebars));

        pippo.setServer(new JzonbieJettyServer());
        pippo.getServer().setPort(options.getPort()).getSettings().host("0.0.0.0");
        pippo.start();

        port = pippo.getServer().getPort();
    }

    public int getPort() {
        return port;
    }

    @Override
    public ZombiePriming prime(AppRequest request, AppResponse response) {
        final ZombiePriming zombiePriming = new ZombiePriming(request, response);
        final ZombiePriming deserialized = normalizeForPriming(zombiePriming, ZombiePriming.class);
        primingContext.add(deserialized);
        return deserialized;
    }

    @Override
    public List<PrimedMapping> prime(File file) {
        try {
            final String mappingsString = IoUtils.toString(new FileInputStream(file));
            final List<PrimedMapping> primedMappings = deserializer.deserializeCollection(mappingsString, PrimedMapping.class);
            primedMappingUploader.upload(primedMappings);
            return primedMappings;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ZombiePriming prime(AppRequest request, DefaultAppResponse defaultAppResponse) {
        final AppRequest appRequest = normalizeForPriming(request, AppRequest.class);

        if(defaultAppResponse instanceof StaticDefaultAppResponse) {
            primingContext.addDefault(appRequest, normalizeStaticDefault(defaultAppResponse));
            return new ZombiePriming(request, defaultAppResponse.getResponse());
        }

        primingContext.addDefault(appRequest, defaultAppResponse);
        return new ZombiePriming(request, null);
    }

    private DefaultAppResponse normalizeStaticDefault(DefaultAppResponse defaultAppResponse) {
        return normalizeForPriming(defaultAppResponse, StaticDefaultAppResponse.class);
    }

    private <T> T normalizeForPriming(T appRequest, Class<? extends T> clazz) {
        try {
            return deserializer.deserialize(objectMapper.writeValueAsString(appRequest), clazz);
        } catch(JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PrimedMapping> getCurrentPriming() {
        return primingContext.getCurrentPriming();
    }

    @Override
    public List<ZombiePriming> getHistory() {
        return callHistory.getEntries();
    }

    @Override
    public List<AppRequest> getFailedRequests() {
        return failedRequests;
    }

    @Override
    public void verify(AppRequest appRequest, InvocationVerificationCriteria criteria) throws VerificationException {
        final int count = callHistory.count(appRequest);
        criteria.verify(count);
    }

    @Override
    public void reset() {
        primingContext.reset();
        callHistory.clear();
        failedRequests.clear();
    }

    public void stop() {
        pippo.stop();
        waitAfterStop.ifPresent(wait -> {
            try {
                Thread.sleep(wait.toMillis());
            } catch (InterruptedException ignored) {}
        });
    }

    public static void main(String[] args) {
        final Jzonbie jzonbie = new Jzonbie(options().withPort(30000));
    }
}