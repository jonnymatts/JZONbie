package com.jonnymatts.jzonbie.priming;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jonnymatts.jzonbie.Request;
import com.jonnymatts.jzonbie.jackson.body.BodyContent;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.util.Deserializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.jonnymatts.jzonbie.jackson.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.jackson.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.jackson.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.jackson.body.StringBodyContent.stringBody;

public class AppRequestFactory {

    private static final TypeReference<List<Object>> LIST_TYPE_REFERENCE = new TypeReference<List<Object>>() {};
    private final Deserializer deserializer;

    public AppRequestFactory(Deserializer deserializer) {
        this.deserializer = deserializer;
    }

    public AppRequest create(Request request) {

        final String bodyString = request.getBody();
        final BodyContent bodyContent = getBodyContent(bodyString);

        final Map<String, Object> primedRequestMap = new HashMap<>();
        primedRequestMap.put("path", request.getPath());
        primedRequestMap.put("method", request.getMethod());
        primedRequestMap.put("queryParams", request.getQueryParams());
        primedRequestMap.put("headers", request.getHeaders());

        final AppRequest deserializedAppRequest = deserializer.deserialize(primedRequestMap, AppRequest.class);
        deserializedAppRequest.setBody(bodyContent);
        return deserializedAppRequest;
    }

    private BodyContent getBodyContent(String bodyString) {
        if(isNullOrEmpty(bodyString)) return null;
        if(isJsonMap(bodyString)) return objectBody(deserializer.deserialize(bodyString));
        if(isJsonArray(bodyString)) return arrayBody(deserializer.deserialize(bodyString, LIST_TYPE_REFERENCE));
        if(isJsonString(bodyString)) return stringBody(bodyString.substring(1, bodyString.length()-1));
        return literalBody(bodyString);
    }

    private boolean isJsonMap(String bodyString) {
        return bodyString.startsWith("{") && bodyString.endsWith("}");
    }

    private boolean isJsonArray(String bodyString) {
        return bodyString.startsWith("[") && bodyString.endsWith("]");
    }

    private boolean isJsonString(String bodyString) {
        return bodyString.startsWith("\"") && bodyString.endsWith("\"");
    }
}