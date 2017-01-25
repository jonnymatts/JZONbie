package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.model.AppRequestFactory;
import com.jonnymatts.jzonbie.model.PrimedMappingFactory;
import com.jonnymatts.jzonbie.model.PrimingContext;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.pippo.PippoApplication;
import com.jonnymatts.jzonbie.requests.AppRequestHandler;
import com.jonnymatts.jzonbie.requests.ZombieRequestHandler;
import com.jonnymatts.jzonbie.util.Deserializer;
import ro.pippo.core.Pippo;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static java.lang.String.format;

public class App {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        final ObjectMapper objectMapper = new ObjectMapper().enable(INDENT_OUTPUT).setSerializationInclusion(NON_NULL);
        final Deserializer deserializer = new Deserializer(objectMapper);
        final PrimingContext primingContext = new PrimingContext();
        final List<ZombiePriming> callHistory = new ArrayList<>();
        final AppRequestFactory appRequestFactory = new AppRequestFactory(deserializer);
        final PrimedMappingFactory primedMappingFactory = new PrimedMappingFactory();

        final AppRequestHandler appRequestHandler = new AppRequestHandler(primingContext, callHistory, appRequestFactory);
        final ZombieRequestHandler zombieRequestHandler = new ZombieRequestHandler(primingContext, callHistory, deserializer, primedMappingFactory);

        final Pippo pippo = new Pippo(new PippoApplication(appRequestHandler, zombieRequestHandler, objectMapper));
        pippo.getServer().setPort(PORT);
        pippo.start();

        System.out.println(format("Started server on port: %s", PORT));
    }
}