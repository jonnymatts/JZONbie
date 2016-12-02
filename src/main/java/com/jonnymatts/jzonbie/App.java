package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import spark.Request;
import spark.Response;

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
        final JsonDeserializer jsonDeserializer = new JsonDeserializer(objectMapper);
        final Multimap<PrimedRequest, PrimedResponse> primingContext = LinkedListMultimap.create();
        final PrimedRequestFactory primedRequestFactory = new PrimedRequestFactory(jsonDeserializer);
        final PrimedRequestsFactory primedRequestsFactory = new PrimedRequestsFactory();

        final AppRequestHandler appRequestHandler = new AppRequestHandler(primingContext, primedRequestFactory, objectMapper);

        final ZombieRequestHandler zombieRequestHandler = new ZombieRequestHandler(primingContext, jsonDeserializer, objectMapper, primedRequestsFactory);

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
