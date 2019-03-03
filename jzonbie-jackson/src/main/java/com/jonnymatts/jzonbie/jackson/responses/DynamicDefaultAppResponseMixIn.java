package com.jonnymatts.jzonbie.jackson.responses;

import com.fasterxml.jackson.annotation.JsonValue;

public abstract class DynamicDefaultAppResponseMixIn {

    @JsonValue
    public abstract String serialize();
}