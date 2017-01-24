package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import spark.Response;

public interface RequestHandler {

    Object handle(Request request, Response response) throws JsonProcessingException;
}
