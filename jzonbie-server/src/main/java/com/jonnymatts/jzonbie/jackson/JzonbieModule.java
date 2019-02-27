package com.jonnymatts.jzonbie.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jonnymatts.jzonbie.Body;
import com.jonnymatts.jzonbie.jackson.body.BodyDeserializer;
import com.jonnymatts.jzonbie.jackson.body.BodySerializer;
import com.jonnymatts.jzonbie.responses.DefaultAppResponse;

public class JzonbieModule extends SimpleModule {
    public JzonbieModule() {
        super("jzonbie");
        addSerializer(Body.class, new BodySerializer());
        addDeserializer(Body.class, new BodyDeserializer());
        addDeserializer(DefaultAppResponse.class, new DefaultAppResponseDeserializer());
    }
}