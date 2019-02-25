package com.jonnymatts.jzonbie.jackson.responses;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.*;
import com.jonnymatts.jzonbie.body.BodyContent;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.AppResponseBuilder;
import com.jonnymatts.jzonbie.responses.DefaultAppResponse;
import com.jonnymatts.jzonbie.responses.DefaultingQueue;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.jonnymatts.jzonbie.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.body.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.jackson.body.BodyContentMixIn.TYPE_IDENTIFIER;
import static com.jonnymatts.jzonbie.responses.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class DefaultingQueueDeserializer extends StdDeserializer<DefaultingQueue> {

    public DefaultingQueueDeserializer() {
        this(null);
    }

    public DefaultingQueueDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public DefaultingQueue deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        final JsonNode defaultNode = node.get("default");
        final DefaultAppResponse defaultAppResponse = (defaultNode == null || defaultNode instanceof NullNode || defaultNode instanceof TextNode) ? null
                : staticDefault(convertObjectNodeToAppResponse(defaultNode));

        final List<AppResponse> appResponses = StreamSupport.stream(node.get("primed").spliterator(), false)
                .map(this::convertObjectNodeToAppResponse)
                .collect(toList());

        final DefaultingQueue defaultingQueue = new DefaultingQueue();
        defaultingQueue.add(appResponses);
        defaultingQueue.setDefault(defaultAppResponse);
        return defaultingQueue;
    }

    private AppResponse convertObjectNodeToAppResponse(JsonNode queueNode) {
        final AppResponseBuilder builder = AppResponse.builder(queueNode.get("statusCode").intValue());
        final BodyContent body = getBodyContent(queueNode.get("body"));
        if(body != null) {
            builder.withBody(body);
        }
        final Map<String, String> headers = (Map<String, String>) convertJsonNodeToObject(queueNode.get("headers"));
        if(headers != null) {
            headers.entrySet().forEach(e -> builder.withHeader(e.getKey(), e.getValue()));
        }
        return builder.build();
    }

    // TODO: Handle null here
    private BodyContent getBodyContent(JsonNode bodyNode) {
        final Object object = convertJsonNodeToObject(bodyNode);
        if(object == null) return null;
        if(bodyNode instanceof ObjectNode) {
            final Map<String, Object> map = (Map<String, Object>) object;
            map.remove(TYPE_IDENTIFIER);
            return objectBody(map);
        } else {
            final List<Object> list = (List<Object>) object;
            final String bodyContentType = (String)list.get(0);
            switch (bodyContentType) {
                case "J_STRING":
                    return stringBody((String)list.get(1));
                case "J_ARRAY":
                    list.remove(0);
                    return arrayBody((List<Object>)list.get(0));
                default:
                    return literalBody(list.get(1));
            }
        }
    }

    private Map<String, Object> getMapFromObjectNode(ObjectNode objectNode) {
        final Map<String, Object> map = iteratorToStream(objectNode.fields()).collect(
                toMap(
                        Map.Entry::getKey,
                        e -> convertJsonNodeToObject(e.getValue())
                )
        );
        return map;
    }

    private List<Object> getListFromArrayNode(ArrayNode arrayNode) {
        return iteratorToStream(arrayNode.elements())
                .map(this::convertJsonNodeToObject)
                .collect(toList());
    }

    private Object convertJsonNodeToObject(JsonNode node) {
        if(node == null) return null;
        if(node instanceof NullNode) return null;
        if(node instanceof TextNode) return node.textValue();
        if(node instanceof IntNode) return node.intValue();
        if(node instanceof LongNode) return node.longValue();
        if(node instanceof BooleanNode) return node.booleanValue();
        if(node instanceof ArrayNode) return getListFromArrayNode((ArrayNode) node);
        if(node instanceof ObjectNode) return getMapFromObjectNode((ObjectNode) node);
        throw new RuntimeException("Unknown node type: " + node.getNodeType());
    }

    private static <T> Stream<T> iteratorToStream(final Iterator<T> iterator) {
        Iterable<T> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}