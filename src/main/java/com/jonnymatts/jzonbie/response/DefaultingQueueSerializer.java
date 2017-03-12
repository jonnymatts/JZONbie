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
        writeDefaultField(gen, value);
        gen.writeArrayFieldStart("primed");
        for(AppResponse appResponse : value.getEntries()) {
            gen.writeObject(appResponse);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    private void writeDefaultField(JsonGenerator gen, DefaultingQueue<AppResponse> value) throws IOException {
        final DefaultResponse<AppResponse> defaultResponse = value.getDefault().orElse(null);

        if(defaultResponse == null) {
            gen.writeObjectField("default", null);
        } else if(!defaultResponse.isDynamic()) {
            gen.writeObjectField("default", defaultResponse.getResponse());
        } else {
            gen.writeStringField("default", "Dynamic default generator");
        }
    }
}