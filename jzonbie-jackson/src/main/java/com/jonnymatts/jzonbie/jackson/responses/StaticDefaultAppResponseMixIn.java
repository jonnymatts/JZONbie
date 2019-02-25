package com.jonnymatts.jzonbie.jackson.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jonnymatts.jzonbie.responses.AppResponse;

public abstract class StaticDefaultAppResponseMixIn {

    @JsonCreator
    public StaticDefaultAppResponseMixIn(@JsonProperty("response") AppResponse response) { }
}