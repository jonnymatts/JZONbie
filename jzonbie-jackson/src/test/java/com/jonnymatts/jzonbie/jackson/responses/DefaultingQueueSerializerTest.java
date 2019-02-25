package com.jonnymatts.jzonbie.jackson.responses;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.DefaultingQueue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.jonnymatts.jzonbie.responses.DefaultAppResponse.DynamicDefaultAppResponse.dynamicDefault;
import static com.jonnymatts.jzonbie.responses.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DefaultingQueueSerializerTest {

    @Mock private JsonGenerator jsonGenerator;
    @Mock private SerializerProvider provider;

    @Test
    public void serializeSerializesStaticDefaultAppResponseAsObject() throws Exception {
        final AppResponse response = AppResponse.builder(200).build();
        final DefaultingQueue queue = new DefaultingQueue(){{
            setDefault(staticDefault(response));
        }};

        final DefaultingQueueSerializer serializer = new DefaultingQueueSerializer();

        serializer.serialize(queue, jsonGenerator, provider);

        verify(jsonGenerator).writeObjectField("default", response);
    }

    @Test
    public void serializeSerializesDynamicDefaultAppResponseAsString() throws Exception {
        final AppResponse response = AppResponse.builder(200).build();
        final DefaultingQueue queue = new DefaultingQueue(){{
            setDefault(dynamicDefault(() -> response));
        }};

        final DefaultingQueueSerializer serializer = new DefaultingQueueSerializer();

        serializer.serialize(queue, jsonGenerator, provider);

        verify(jsonGenerator).writeStringField("default", "Dynamic default generator");
    }
}