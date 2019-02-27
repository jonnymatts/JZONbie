package com.jonnymatts.jzonbie.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.DefaultAppResponse;

import java.io.IOException;

public class DefaultAppResponseDeserializer extends StdDeserializer<DefaultAppResponse> {

    public DefaultAppResponseDeserializer() {
        this(null);
    }

    public DefaultAppResponseDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public DefaultAppResponse deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        final AppResponse appResponse = p.readValueAs(AppResponse.class);

        return null;
    }
}