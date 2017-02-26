package com.jonnymatts.jzonbie.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jonnymatts.jzonbie.model.AppResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultingQueueDeserializerTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private JsonParser jsonParser;
    @Mock
    private DeserializationContext context;

    @Test
    public void deserializeReturnsEmptyQueueIfArrayIsEmpty() throws Exception {
        final DefaultingQueueDeserializer deserializer = new DefaultingQueueDeserializer();
        final ArrayNode arrayNode = new ArrayNode(instance, emptyList());

        when(jsonParser.getCodec().readTree(jsonParser)).thenReturn(arrayNode);

        final DefaultingQueue<AppResponse> got = deserializer.deserialize(jsonParser, context);

        assertThat(got.hasSize()).isEqualTo(0);
    }

    @Test
    public void deserializeReturnsQueueWithItemIfArrayContainsAppResponse() throws Exception {
        final DefaultingQueueDeserializer deserializer = new DefaultingQueueDeserializer();
        final ArrayNode arrayNode = new ArrayNode(instance, singletonList(
                new ObjectNode(instance,
                        new HashMap<String, JsonNode>() {{
                            put("statusCode", new IntNode(200));
                            put("body", new ObjectNode(instance, singletonMap("array", new ArrayNode(instance))));
                            put("headers", NullNode.instance);
                        }}
                )
        ));

        when(jsonParser.getCodec().readTree(jsonParser)).thenReturn(arrayNode);

        final DefaultingQueue<AppResponse> got = deserializer.deserialize(jsonParser, context);

        final AppResponse expectedResponse = AppResponse.builder(200)
                .withBody(singletonMap("array", emptyList()))
                .build();

        assertThat(got.getEntries()).containsOnly(expectedResponse);
    }
}