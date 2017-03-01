package com.jonnymatts.jzonbie.response;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.response.DefaultResponse.DynamicDefaultResponse;
import com.jonnymatts.jzonbie.response.DefaultResponse.StaticDefaultResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DefaultingQueueSerializerTest {

    @Mock private JsonGenerator jsonGenerator;
    @Mock private SerializerProvider provider;

    @Test
    public void serializeSerializesStaticDefaultAppResponseAsObject() throws Exception {
        final AppResponse response = AppResponse.builder(200).build();
        final DefaultingQueue<AppResponse> queue = new DefaultingQueue<AppResponse>(){{
            setDefault(new StaticDefaultResponse<>(response));
        }};

        final DefaultingQueueSerializer serializer = new DefaultingQueueSerializer();

        serializer.serialize(queue, jsonGenerator, provider);

        verify(jsonGenerator).writeObjectField("default", response);
    }

    @Test
    public void serializeSerializesDynamicDefaultAppResponseAsString() throws Exception {
        final AppResponse response = AppResponse.builder(200).build();
        final DefaultingQueue<AppResponse> queue = new DefaultingQueue<AppResponse>(){{
            setDefault(new DynamicDefaultResponse<>(() -> response));
        }};

        final DefaultingQueueSerializer serializer = new DefaultingQueueSerializer();

        serializer.serialize(queue, jsonGenerator, provider);

        verify(jsonGenerator).writeStringField("default", "Dynamic default generator");
    }
}