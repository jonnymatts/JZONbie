package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.client.JzonbieClient;
import com.jonnymatts.jzonbie.jetty.JzonbieJettyServer;
import com.jonnymatts.jzonbie.model.*;
import com.jonnymatts.jzonbie.pippo.PippoApplication;
import com.jonnymatts.jzonbie.requests.AppRequestHandler;
import com.jonnymatts.jzonbie.requests.ZombieRequestHandler;
import com.jonnymatts.jzonbie.util.Deserializer;
import ro.pippo.core.Pippo;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

public class Jzonbie implements JzonbieClient {

    private final ObjectMapper objectMapper = new ObjectMapper().enable(INDENT_OUTPUT).setSerializationInclusion(NON_NULL);
    private final Deserializer deserializer = new Deserializer(objectMapper);
    private final PrimingContext primingContext = new PrimingContext();
    private final List<ZombiePriming> callHistory = new ArrayList<>();
    private final AppRequestFactory appRequestFactory = new AppRequestFactory(deserializer);
    private final PrimedMappingFactory primedMappingFactory = new PrimedMappingFactory();
    private final AppRequestHandler appRequestHandler = new AppRequestHandler(primingContext, callHistory, appRequestFactory);
    private final ZombieRequestHandler zombieRequestHandler = new ZombieRequestHandler(primingContext, callHistory, deserializer, primedMappingFactory);
    private final Pippo pippo = new Pippo(new PippoApplication(appRequestHandler, zombieRequestHandler, objectMapper));

    public Jzonbie() {
        this(0);
    }

    public Jzonbie(int port) {
        pippo.setServer(new JzonbieJettyServer());
        pippo.getServer().setPort(port).getSettings().host("0.0.0.0");
        pippo.start();
    }

    public int getPort() {
        return pippo.getServer().getPort();
    }

    @Override
    public ZombiePriming primeZombie(AppRequest request, AppResponse response) {
        final ZombiePriming zombiePriming = new ZombiePriming(request, response);
        primingContext.add(zombiePriming);
        return zombiePriming;
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
    }
}