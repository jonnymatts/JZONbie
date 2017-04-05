package com.jonnymatts.jzonbie.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.jonnymatts.jzonbie.model.AppResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;
import static com.jonnymatts.jzonbie.model.content.StringBodyContent.stringBody;
import static java.util.Arrays.asList;
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
        put("body", new ObjectNode(instance, new HashMap<String, JsonNode>() {{
            put("array", new ArrayNode(instance));
            put("JZONBIE_CONTENT_TYPE", new TextNode("J_OBJECT"));
        }}));
        put("headers", NullNode.instance);
    }};

    @Test
    public void deserializeReturnsEmptyQueueIfResponsesIsEmpty() throws Exception {
        final DefaultingQueueDeserializer deserializer = new DefaultingQueueDeserializer();
        final ObjectNode queueNode = new ObjectNode(instance,  new HashMap<String, JsonNode>(){{
            put("default", NullNode.instance);
            put("primed", new ArrayNode(instance, emptyList()));
        }});

        when(jsonParser.getCodec().readTree(jsonParser)).thenReturn(queueNode);

        final DefaultingQueue got = deserializer.deserialize(jsonParser, context);

        assertThat(got.getDefault()).isEmpty();
        assertThat(got.hasSize()).isEqualTo(0);
    }

    @Test
    public void deserializeReturnsQueueWithItemIfResponsesContainsAppResponse() throws Exception {
        final DefaultingQueueDeserializer deserializer = new DefaultingQueueDeserializer();
        final ArrayNode arrayNode = new ArrayNode(instance, singletonList(new ObjectNode(instance, APP_RESPONSE_NODE_MAP)));

        final ObjectNode queueNode = new ObjectNode(instance,  new HashMap<String, JsonNode>(){{
            put("default", NullNode.instance);
            put("primed", arrayNode);
        }});

        when(jsonParser.getCodec().readTree(jsonParser)).thenReturn(queueNode);

        final DefaultingQueue got = deserializer.deserialize(jsonParser, context);

        assertThat(got.getDefault()).isEmpty();
        assertThat(got.getEntries()).containsOnly(EXPECTED_RESPONSE);
    }

    @Test
    public void deserializeReturnsQueueWithDefaultIfDefaultIsPresent() throws Exception {
        final DefaultingQueueDeserializer deserializer = new DefaultingQueueDeserializer();
        final ObjectNode queueNode = new ObjectNode(instance,  new HashMap<String, JsonNode>(){{
            put("default", new ObjectNode(instance, APP_RESPONSE_NODE_MAP));
            put("primed", new ArrayNode(instance, emptyList()));
        }});

        when(jsonParser.getCodec().readTree(jsonParser)).thenReturn(queueNode);

        final DefaultingQueue got = deserializer.deserialize(jsonParser, context);

        assertThat(got.getDefault().map(DefaultAppResponse::getResponse)).contains(EXPECTED_RESPONSE);
        assertThat(got.hasSize()).isEqualTo(0);
    }

    @Test
    public void deserializeReturnsQueueWhenValueFromNodeIsNull() throws Exception {
        final DefaultingQueueDeserializer deserializer = new DefaultingQueueDeserializer();
        final HashMap<String, JsonNode> copy = new HashMap<>(APP_RESPONSE_NODE_MAP);
        copy.put("headers", null);

        final ObjectNode queueNode = new ObjectNode(instance,  new HashMap<String, JsonNode>(){{
            put("default", new ObjectNode(instance, copy));
            put("primed", new ArrayNode(instance, emptyList()));
        }});

        when(jsonParser.getCodec().readTree(jsonParser)).thenReturn(queueNode);

        final DefaultingQueue got = deserializer.deserialize(jsonParser, context);

        assertThat(got.getDefault().map(DefaultAppResponse::getResponse)).contains(EXPECTED_RESPONSE);
        assertThat(got.hasSize()).isEqualTo(0);
    }

    @Test
    public void deserializeReturnsQueueWhenBodyIsLiteralContentBody() throws Exception {
        final String requestString = "requestString";
        final HashMap<String, JsonNode> copy = new HashMap<>(APP_RESPONSE_NODE_MAP);
        final ObjectNode queueNode = new ObjectNode(instance,  new HashMap<String, JsonNode>(){{
            put("default", new ObjectNode(instance, copy));
            put("primed", new ArrayNode(instance, emptyList()));
        }});

        copy.put("body", new ArrayNode(instance, asList(new TextNode("J_LITERAL"), new TextNode(requestString))));

        when(jsonParser.getCodec().readTree(jsonParser)).thenReturn(queueNode);

        final DefaultingQueueDeserializer deserializer = new DefaultingQueueDeserializer();

        final DefaultingQueue got = deserializer.deserialize(jsonParser, context);

        final AppResponse expectedAppResponse = AppResponse.builder(200)
                .withBody(requestString)
                .build();

        assertThat(got.getDefault().map(DefaultAppResponse::getResponse)).contains(expectedAppResponse);
        assertThat(got.hasSize()).isEqualTo(0);
    }

    @Test
    public void deserializeReturnsQueueWhenBodyIsJsonStringContentBody() throws Exception {
        final String requestString = "requestString";
        final HashMap<String, JsonNode> copy = new HashMap<>(APP_RESPONSE_NODE_MAP);
        final ObjectNode queueNode = new ObjectNode(instance,  new HashMap<String, JsonNode>(){{
            put("default", new ObjectNode(instance, copy));
            put("primed", new ArrayNode(instance, emptyList()));
        }});

        copy.put("body", new ArrayNode(instance, asList(new TextNode("J_STRING"), new TextNode(requestString))));

        when(jsonParser.getCodec().readTree(jsonParser)).thenReturn(queueNode);

        final DefaultingQueueDeserializer deserializer = new DefaultingQueueDeserializer();

        final DefaultingQueue got = deserializer.deserialize(jsonParser, context);

        final AppResponse expectedAppResponse = AppResponse.builder(200)
                .withBody(stringBody(requestString))
                .build();

        assertThat(got.getDefault().map(DefaultAppResponse::getResponse)).contains(expectedAppResponse);
        assertThat(got.hasSize()).isEqualTo(0);
    }

    @Test
    public void deserializeReturnsQueueWhenBodyIsListContentBody() throws Exception {
        final List<String> requestList = singletonList("requestString");
        final HashMap<String, JsonNode> copy = new HashMap<>(APP_RESPONSE_NODE_MAP);
        final ObjectNode queueNode = new ObjectNode(instance,  new HashMap<String, JsonNode>(){{
            put("default", new ObjectNode(instance, copy));
            put("primed", new ArrayNode(instance, emptyList()));
        }});

        copy.put("body", new ArrayNode(instance, asList(new TextNode("J_ARRAY"), new ArrayNode(instance, singletonList(new TextNode("requestString"))))));

        when(jsonParser.getCodec().readTree(jsonParser)).thenReturn(queueNode);

        final DefaultingQueueDeserializer deserializer = new DefaultingQueueDeserializer();

        final DefaultingQueue got = deserializer.deserialize(jsonParser, context);

        final AppResponse expectedAppResponse = AppResponse.builder(200)
                .withBody(requestList)
                .build();

        assertThat(got.getDefault().map(DefaultAppResponse::getResponse)).contains(expectedAppResponse);
        assertThat(got.hasSize()).isEqualTo(0);
    }

    @Test
    public void deserializeReturnsQueueWhenDefaultAndPrimedBodiesAreNull() throws Exception {
        final HashMap<String, JsonNode> copy = new HashMap<>(APP_RESPONSE_NODE_MAP);
        final ArrayNode arrayNode = new ArrayNode(instance, singletonList(new ObjectNode(instance, copy)));
        final ObjectNode queueNode = new ObjectNode(instance,  new HashMap<String, JsonNode>(){{
            put("default", new ObjectNode(instance, copy));
            put("primed", arrayNode);
        }});

        copy.put("body", null);

        when(jsonParser.getCodec().readTree(jsonParser)).thenReturn(queueNode);

        final DefaultingQueueDeserializer deserializer = new DefaultingQueueDeserializer();

        final DefaultingQueue got = deserializer.deserialize(jsonParser, context);

        final AppResponse expectedAppResponse = AppResponse.builder(200).build();

        assertThat(got.getDefault().map(DefaultAppResponse::getResponse)).contains(expectedAppResponse);
        assertThat(got.hasSize()).isEqualTo(1);

        final AppResponse appResponse = got.getEntries().get(0);

        assertThat(appResponse.getBody()).isNull();
        assertThat(appResponse.getStatusCode()).isEqualTo(200);
    }
}