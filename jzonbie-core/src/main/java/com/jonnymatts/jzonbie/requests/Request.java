package com.jonnymatts.jzonbie.requests;

import java.util.List;
import java.util.Map;

public interface Request {
    String getPath();
    String getMethod();
    Map<String, String> getHeaders();
    String getBody();
    Map<String, List<String>> getQueryParams();
    String getPrimingFileContent();
}
