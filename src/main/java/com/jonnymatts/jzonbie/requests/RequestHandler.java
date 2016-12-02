package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import spark.Request;
import spark.Response;

public interface RequestHandler {

    String handle(Request request, Response response) throws JsonProcessingException;
}
