package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.jonnymatts.jzonbie.model.*;
import com.jonnymatts.jzonbie.requests.AppRequestHandler;
import com.jonnymatts.jzonbie.requests.RequestHandler;
import com.jonnymatts.jzonbie.requests.ZombieRequestHandler;
import com.jonnymatts.jzonbie.spark.JsonResponseTransformer;
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
    private static JsonResponseTransformer jsonResponseTransformer;

    public App(AppRequestHandler appRequestHandler,
               ZombieRequestHandler zombieRequestHandler,
               JsonResponseTransformer jsonResponseTransformer) {
        App.appRequestHandler = appRequestHandler;
        App.zombieRequestHandler = zombieRequestHandler;
        App.jsonResponseTransformer = jsonResponseTransformer;
    }

    public static void main(String[] args) {
        final ObjectMapper objectMapper = new ObjectMapper().enable(INDENT_OUTPUT).setSerializationInclusion(NON_NULL);
        final Deserializer deserializer = new Deserializer(objectMapper);
        final Multimap<PrimedRequest, PrimedResponse> primingContext = LinkedListMultimap.create();
        final List<JZONbieRequest> callHistory = new ArrayList<>();
        final PrimedRequestFactory primedRequestFactory = new PrimedRequestFactory(deserializer);
        final PrimedMappingFactory primedMappingFactory = new PrimedMappingFactory();

        final AppRequestHandler appRequestHandler = new AppRequestHandler(primingContext, callHistory, primedRequestFactory);
        final ZombieRequestHandler zombieRequestHandler = new ZombieRequestHandler(primingContext, callHistory, deserializer, primedMappingFactory);
        final JsonResponseTransformer jsonResponseTransformer = new JsonResponseTransformer(objectMapper);

        new App(appRequestHandler, zombieRequestHandler, jsonResponseTransformer);

        init();

        System.out.println(format("Started server on port: %s", PORT));
    }

    private static void init() {
        port(PORT);

        get("*", App::handleRequest, jsonResponseTransformer);
        post("*", App::handleRequest, jsonResponseTransformer);
        patch("*", App::handleRequest, jsonResponseTransformer);
        put("*", App::handleRequest, jsonResponseTransformer);
        delete("*", App::handleRequest, jsonResponseTransformer);
        head("*", App::handleRequest, jsonResponseTransformer);
        options("*", App::handleRequest, jsonResponseTransformer);
    }

    private static Object handleRequest(Request request, Response response) {
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
