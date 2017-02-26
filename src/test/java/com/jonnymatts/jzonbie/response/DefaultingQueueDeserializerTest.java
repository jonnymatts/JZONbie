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
import java.util.Map;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultingQueueDeserializerTest {

    @Mock(answer = RETURNS_DEEP_STUBS) private JsonParser jsonParser;
    @Mock private DeserializationContext context;
    private static final AppResponse EXPECTED_RESPONSE = AppResponse.builder(200)
            .withBody(singletonMap("array", emptyList()))
            .build();
    private static final Map<String, JsonNode> APP_RESPONSE_NODE_MAP = new HashMap<String, JsonNode>() {{
        put("statusCode", new IntNode(200));
        put("body", new ObjectNode(instance, singletonMap("array", new ArrayNode(instance))));
        put("headers", NullNode.instance);
    }};

    @Test
    public void deserializeReturnsEmptyQueueIfResponsesIsEmpty() throws Exception {
        final DefaultingQueueDeserializer deserializer = new DefaultingQueueDeserializer();
        final ObjectNode queueNode = new ObjectNode(instance,  new HashMap<String, JsonNode>(){{
            put("default", NullNode.instance);
            put("responses", new ArrayNode(instance, emptyList()));
        }});

        when(jsonParser.getCodec().readTree(jsonParser)).thenReturn(queueNode);

        final DefaultingQueue<AppResponse> got = deserializer.deserialize(jsonParser, context);

        assertThat(got.getDefault()).isEmpty();
        assertThat(got.hasSize()).isEqualTo(0);
    }

    @Test
    public void deserializeReturnsQueueWithItemIfResponsesContainsAppResponse() throws Exception {
        final DefaultingQueueDeserializer deserializer = new DefaultingQueueDeserializer();
        final ArrayNode arrayNode = new ArrayNode(instance, singletonList(new ObjectNode(instance, APP_RESPONSE_NODE_MAP)));

        final ObjectNode queueNode = new ObjectNode(instance,  new HashMap<String, JsonNode>(){{
            put("default", NullNode.instance);
            put("responses", arrayNode);
        }});

        when(jsonParser.getCodec().readTree(jsonParser)).thenReturn(queueNode);

        final DefaultingQueue<AppResponse> got = deserializer.deserialize(jsonParser, context);

        assertThat(got.getDefault()).isEmpty();
        assertThat(got.getEntries()).containsOnly(EXPECTED_RESPONSE);
    }

    @Test
    public void deserializeReturnsQueueWithDefaultIfDefaultIsPresent() throws Exception {
        final DefaultingQueueDeserializer deserializer = new DefaultingQueueDeserializer();
        final ObjectNode queueNode = new ObjectNode(instance,  new HashMap<String, JsonNode>(){{
            put("default", new ObjectNode(instance, APP_RESPONSE_NODE_MAP));
            put("responses", new ArrayNode(instance, emptyList()));
        }});

        when(jsonParser.getCodec().readTree(jsonParser)).thenReturn(queueNode);

        final DefaultingQueue<AppResponse> got = deserializer.deserialize(jsonParser, context);

        assertThat(got.getDefault()).contains(EXPECTED_RESPONSE);
        assertThat(got.hasSize()).isEqualTo(0);
    }

    @Test
    public void deserializeReturnsQueueWhenValueFromNodeIsNull() throws Exception {
        final DefaultingQueueDeserializer deserializer = new DefaultingQueueDeserializer();
        final HashMap<String, JsonNode> copy = new HashMap<>(APP_RESPONSE_NODE_MAP);
        copy.put("headers", null);

        final ObjectNode queueNode = new ObjectNode(instance,  new HashMap<String, JsonNode>(){{
            put("default", new ObjectNode(instance, copy));
            put("responses", new ArrayNode(instance, emptyList()));
        }});

        when(jsonParser.getCodec().readTree(jsonParser)).thenReturn(queueNode);

        final DefaultingQueue<AppResponse> got = deserializer.deserialize(jsonParser, context);

        assertThat(got.getDefault()).contains(EXPECTED_RESPONSE);
        assertThat(got.hasSize()).isEqualTo(0);
    }
}