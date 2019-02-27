package com.jonnymatts.jzonbie.jackson.body;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.jonnymatts.jzonbie.Body;

import java.io.IOException;

import static com.jonnymatts.jzonbie.jackson.body.BodyContent.TYPE_IDENTIFIER;

public class BodySerializer extends StdSerializer<Body> {

    public BodySerializer() {
        this(null);
    }

    public BodySerializer(Class<Body> t) {
        super(t);
    }

    @Override
    public void serialize(Body value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        final BodyContent bodyContent = (BodyContent) value;
        gen.writeStartObject();
        gen.writeStringField(TYPE_IDENTIFIER, bodyContent.getType().name());
        gen.writeObjectField("content", value.getContent());
        gen.writeEndObject();
    }
}