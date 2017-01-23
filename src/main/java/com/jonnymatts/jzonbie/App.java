package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.jonnymatts.jzonbie.model.*;
import com.jonnymatts.jzonbie.repsonse.ErrorResponse;
import com.jonnymatts.jzonbie.repsonse.PrimingNotFoundErrorResponse;
import com.jonnymatts.jzonbie.requests.AppRequestHandler;
import com.jonnymatts.jzonbie.requests.PrimingNotFoundException;
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
import static org.eclipse.jetty.http.HttpStatus.INTERNAL_SERVER_ERROR_500;
import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;
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
        final Multimap<ZombieRequest, ZombieResponse> primingContext = LinkedListMultimap.create();
        final List<ZombiePriming> callHistory = new ArrayList<>();
        final ZombieRequestFactory zombieRequestFactory = new ZombieRequestFactory(deserializer);
        final PrimedMappingFactory primedMappingFactory = new PrimedMappingFactory();

        final AppRequestHandler appRequestHandler = new AppRequestHandler(primingContext, callHistory, zombieRequestFactory);
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
        }
        catch (PrimingNotFoundException e) {
            response.status(NOT_FOUND_404);
            response.header("Content-Type", "application/json");
            return new PrimingNotFoundErrorResponse(e.getRequest());
        }
        catch (Exception e) {
            response.status(INTERNAL_SERVER_ERROR_500);
            response.header("Content-Type", "application/json");
            return new ErrorResponse(format("Error occurred: %s - %s", e.getClass().getName(), e.getMessage()));
        }
    }
}
