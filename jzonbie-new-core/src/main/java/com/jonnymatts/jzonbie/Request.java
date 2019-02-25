package com.jonnymatts.jzonbie;

import java.util.List;
import java.util.Map;

public interface Request {

    String getPath();

    String getMethod();

    Map<String, String> getHeaders();

    Body<?> getBody();

    Map<String, List<String>> getQueryParams();

}
