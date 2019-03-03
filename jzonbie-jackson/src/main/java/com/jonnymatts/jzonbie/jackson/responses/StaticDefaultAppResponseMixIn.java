package com.jonnymatts.jzonbie.jackson.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.jonnymatts.jzonbie.responses.AppResponse;

public abstract class StaticDefaultAppResponseMixIn {

    @JsonCreator
    public StaticDefaultAppResponseMixIn(AppResponse response) { }

    @JsonValue
    public abstract AppResponse getResponse();
}