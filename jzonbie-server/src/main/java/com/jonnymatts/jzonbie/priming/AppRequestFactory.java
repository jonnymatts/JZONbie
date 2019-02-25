package com.jonnymatts.jzonbie.priming;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jonnymatts.jzonbie.priming.content.BodyContent;
import com.jonnymatts.jzonbie.requests.Request;
import com.jonnymatts.jzonbie.util.Deserializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.jonnymatts.jzonbie.priming.content.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.priming.content.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.priming.content.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.priming.content.StringBodyContent.stringBody;

public class AppRequestFactory {

    private final Deserializer deserializer;

    public AppRequestFactory(Deserializer deserializer) {
        this.deserializer = deserializer;
    }

    public AppRequest create(Request request) {

        final String bodyString = request.getBody();
        final BodyContent bodyContent = getBodyContent(bodyString);

        final Map<String, Object> primedRequestMap = new HashMap<String, Object>(){{
            put("path", request.getPath());
            put("method", request.getMethod());
            put("queryParams", request.getQueryParams());
            put("headers", request.getHeaders());
        }};

        final AppRequest deserializedAppRequest = deserializer.deserialize(primedRequestMap, AppRequest.class);
        deserializedAppRequest.setBody(bodyContent);
        return deserializedAppRequest;
    }

    private BodyContent getBodyContent(String bodyString) {
        if(isNullOrEmpty(bodyString)) return null;
        if(isJsonMap(bodyString)) return objectBody(deserializer.deserialize(bodyString));
        if(isJsonArray(bodyString)) return arrayBody(deserializer.deserialize(bodyString, new TypeReference<List<Object>>() {}));
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