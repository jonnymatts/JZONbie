package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.client.JzonbieClient;
import com.jonnymatts.jzonbie.jetty.JzonbieJettyServer;
import com.jonnymatts.jzonbie.model.*;
import com.jonnymatts.jzonbie.pippo.PippoApplication;
import com.jonnymatts.jzonbie.requests.AppRequestHandler;
import com.jonnymatts.jzonbie.requests.ZombieRequestHandler;
import com.jonnymatts.jzonbie.response.CurrentPrimingFileResponseFactory;
import com.jonnymatts.jzonbie.response.DefaultAppResponse;
import com.jonnymatts.jzonbie.response.DefaultAppResponse.StaticDefaultAppResponse;
import com.jonnymatts.jzonbie.util.Deserializer;
import ro.pippo.core.Pippo;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.jonnymatts.jzonbie.JzonbieOptions.options;

public class Jzonbie implements JzonbieClient {

    private final PrimingContext primingContext = new PrimingContext();
    private final List<ZombiePriming> callHistory = new ArrayList<>();
    private final Pippo pippo;
    public Deserializer deserializer;
    public ObjectMapper objectMapper;

    public Jzonbie() {
        this(options());
    }

    public Jzonbie(JzonbieOptions options) {
        objectMapper = options.getObjectMapper().setSerializationInclusion(NON_NULL);
        deserializer = new Deserializer(objectMapper);
        final AppRequestFactory appRequestFactory = new AppRequestFactory(deserializer);
        final CurrentPrimingFileResponseFactory fileResponseFactory = new CurrentPrimingFileResponseFactory(objectMapper);
        final AppRequestHandler appRequestHandler = new AppRequestHandler(primingContext, callHistory, appRequestFactory);
        final ZombieRequestHandler zombieRequestHandler = new ZombieRequestHandler(options, primingContext, callHistory, deserializer, fileResponseFactory);

        pippo = new Pippo(new PippoApplication(options, appRequestHandler, zombieRequestHandler, options.getObjectMapper()));


        pippo.setServer(new JzonbieJettyServer());
        pippo.getServer().setPort(options.getPort()).getSettings().host("0.0.0.0");
        pippo.start();
    }

    public int getPort() {
        return pippo.getServer().getPort();
    }

    @Override
    public ZombiePriming primeZombie(AppRequest request, AppResponse response) {
        final ZombiePriming zombiePriming = new ZombiePriming(request, response);
        final ZombiePriming deserialized = normalizeForPriming(zombiePriming, ZombiePriming.class);
        primingContext.add(deserialized);
        return deserialized;
    }

    @Override
    public ZombiePriming primeZombieForDefault(AppRequest request, DefaultAppResponse defaultAppResponse) {
        final AppRequest appRequest = normalizeForPriming(request, AppRequest.class);
        final DefaultAppResponse appResponse = defaultAppResponse.isDynamic() ? defaultAppResponse :
                normalizeForPriming(defaultAppResponse, StaticDefaultAppResponse.class);

        primingContext.addDefault(appRequest, appResponse);

        final AppResponse returnResponse = defaultAppResponse.isDynamic() ? null : defaultAppResponse.getResponse();
        return new ZombiePriming(request, returnResponse);
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
        return callHistory;
    }

    @Override
    public void reset() {
        primingContext.clear();
        callHistory.clear();
    }

    public void stop() {
        pippo.stop();
        try {
            Thread.sleep(2000); // TODO: This should be made better, though NOTHING has worked so far. And we did a LOT
        } catch(InterruptedException ignored) {}
    }

    public static void main(String[] args) {
        new Jzonbie(options().withPort(30000));
    }
}