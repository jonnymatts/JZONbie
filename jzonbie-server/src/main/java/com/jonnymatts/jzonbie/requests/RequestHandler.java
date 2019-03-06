package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.Request;
import com.jonnymatts.jzonbie.Response;

public interface RequestHandler {

    Response handle(Request request);
}
