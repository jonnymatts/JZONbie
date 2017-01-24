package com.jonnymatts.jzonbie.response;

import java.util.Map;

public interface Response {
    int getStatusCode();
    Map<String, String> getHeaders();
    Object getBody();
}