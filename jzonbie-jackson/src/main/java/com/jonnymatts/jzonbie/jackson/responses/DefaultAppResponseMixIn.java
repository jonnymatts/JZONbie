package com.jonnymatts.jzonbie.jackson.responses;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jonnymatts.jzonbie.responses.defaults.DynamicDefaultAppResponse;
import com.jonnymatts.jzonbie.responses.defaults.StaticDefaultAppResponse;

import static com.jonnymatts.jzonbie.jackson.responses.DefaultAppResponseMixIn.TYPE_IDENTIFIER;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.WRAPPER_OBJECT,
        property = TYPE_IDENTIFIER)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StaticDefaultAppResponse.class, name = "static"),
        @JsonSubTypes.Type(value = DynamicDefaultAppResponse.class, name = "dynamic")
})
public abstract class DefaultAppResponseMixIn {

    static final String TYPE_IDENTIFIER = "JZONBIE_DEFAULT_APP_RESPONSE_TYPE";

}