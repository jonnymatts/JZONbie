package com.jonnymatts.jzonbie.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.*;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.AppResponseBuilder;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class DefaultingQueueDeserializer extends StdDeserializer<DefaultingQueue<AppResponse>> {

    public DefaultingQueueDeserializer() {
        this(null);
    }

    public DefaultingQueueDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public DefaultingQueue<AppResponse> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        final List<AppResponse> appResponses = StreamSupport.stream(node.spliterator(), false)
                .map(appResponseNode -> {
                    final AppResponseBuilder builder = AppResponse.builder(appResponseNode.get("statusCode").intValue())
                            .withBody((Map<String, Object>) convertJsonNodeToObject(appResponseNode.get("body")));
                    final Map<String, String> headers = (Map<String, String>) convertJsonNodeToObject(appResponseNode.get("headers"));
                    if(headers != null) {
                        headers.entrySet().forEach(e -> builder.withHeader(e.getKey(), e.getValue()));
                    }
                    return builder.build();
                })
                .collect(toList());

        return new DefaultingQueue<AppResponse>(){{
            add(appResponses);
        }};
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