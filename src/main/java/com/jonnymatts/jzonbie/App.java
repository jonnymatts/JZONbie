package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.jonnymatts.jzonbie.model.*;
import com.jonnymatts.jzonbie.requests.AppRequestHandler;
import com.jonnymatts.jzonbie.requests.RequestHandler;
import com.jonnymatts.jzonbie.requests.ZombieRequestHandler;
import com.jonnymatts.jzonbie.util.Deserializer;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static java.lang.String.format;
import static spark.Spark.*;

public class App {

    private static final int PORT = 8080;

    private static AppRequestHandler appRequestHandler;
    private static ZombieRequestHandler zombieRequestHandler;

    public App(AppRequestHandler appRequestHandler,
               ZombieRequestHandler zombieRequestHandler) {
        App.appRequestHandler = appRequestHandler;
        App.zombieRequestHandler = zombieRequestHandler;
    }

    public static void main(String[] args) {
        final ObjectMapper objectMapper = new ObjectMapper().enable(INDENT_OUTPUT).setSerializationInclusion(NON_NULL);
        final Deserializer deserializer = new Deserializer(objectMapper);
        final Multimap<PrimedRequest, PrimedResponse> primingContext = LinkedListMultimap.create();
        final List<JZONbieRequest> callHistory = new ArrayList<>();
        final PrimedRequestFactory primedRequestFactory = new PrimedRequestFactory(deserializer);
        final PrimedMappingFactory primedMappingFactory = new PrimedMappingFactory();

        final AppRequestHandler appRequestHandler = new AppRequestHandler(primingContext, callHistory, primedRequestFactory, objectMapper);

        final ZombieRequestHandler zombieRequestHandler = new ZombieRequestHandler(primingContext, callHistory, deserializer, objectMapper, primedMappingFactory);

        new App(appRequestHandler, zombieRequestHandler);

        init();

        System.out.println(format("Started server on port: %s", PORT));
    }

    private static void init() {
        port(PORT);

        get("*", App::handleRequest);
        post("*", App::handleRequest);
        patch("*", App::handleRequest);
        put("*", App::handleRequest);
        delete("*", App::handleRequest);
        head("*", App::handleRequest);
        options("*", App::handleRequest);
    }

    private static String handleRequest(Request request, Response response) {
        final String zombieHeader = request.headers("zombie");

        final RequestHandler requestHandler = zombieHeader != null ?
                zombieRequestHandler : appRequestHandler;

        try {
            return requestHandler.handle(request, response);
        } catch (Exception e) {
            return format("Error occurred: %s - %s", e.getClass().getName(), e.getMessage());
        }
    }
}
