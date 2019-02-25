package com.jonnymatts.jzonbie.jackson.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.DefaultAppResponse.StaticDefaultAppResponse;

public abstract class DefaultAppResponseMixIn {

    @JsonCreator
    public static StaticDefaultAppResponse staticDefault(AppResponse response) {
        return null;
    }

}