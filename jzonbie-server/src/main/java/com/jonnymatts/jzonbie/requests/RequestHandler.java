package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jonnymatts.jzonbie.responses.Response;

public interface RequestHandler {

    Response handle(Request request) throws JsonProcessingException;
}
