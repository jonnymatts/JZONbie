package com.jonnymatts.jzonbie.response;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.jonnymatts.jzonbie.model.AppResponse;

import java.io.IOException;

public class DefaultingQueueSerializer extends StdSerializer<DefaultingQueue<AppResponse>> {

    public DefaultingQueueSerializer() {
        this(null);
    }

    public DefaultingQueueSerializer(Class<DefaultingQueue<AppResponse>> t) {
        super(t);
    }

    @Override
    public void serialize(DefaultingQueue<AppResponse> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("default", value.getDefault().orElse(null));
        gen.writeArrayFieldStart("primed");
        for(AppResponse appResponse : value.getEntries()) {
            gen.writeStartObject();
            gen.writeNumberField("statusCode", appResponse.getStatusCode());
            gen.writeObjectField("headers", appResponse.getHeaders());
            gen.writeObjectField("body", appResponse.getBody());
            gen.writeEndObject();
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }
}