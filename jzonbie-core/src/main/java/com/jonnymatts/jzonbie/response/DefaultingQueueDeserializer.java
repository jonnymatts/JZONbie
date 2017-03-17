package com.jonnymatts.jzonbie.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.*;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.AppResponseBuilder;
import com.jonnymatts.jzonbie.model.content.BodyContent;
import com.jonnymatts.jzonbie.response.DefaultResponse.StaticDefaultResponse;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.jonnymatts.jzonbie.model.content.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.model.content.BodyContent.TYPE_IDENTIFIER;
import static com.jonnymatts.jzonbie.model.content.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.model.content.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.model.content.StringBodyContent.stringBody;
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

        final JsonNode defaultNode = node.get("default");
        final DefaultResponse<AppResponse> defaultResponse = (defaultNode instanceof NullNode || defaultNode instanceof TextNode) ? null
                : new StaticDefaultResponse<>(convertObjectNodeToAppResponse(defaultNode));

        final List<AppResponse> appResponses = StreamSupport.stream(node.get("primed").spliterator(), false)
                .map(this::convertObjectNodeToAppResponse)
                .collect(toList());

        return new DefaultingQueue<AppResponse>(){{
            add(appResponses);
            setDefault(defaultResponse);
        }};
    }

    private AppResponse convertObjectNodeToAppResponse(JsonNode queueNode) {
        final BodyContent body = getBodyContent(queueNode.get("body"));
        final AppResponseBuilder builder = AppResponse.builder(queueNode.get("statusCode").intValue())
                .withBody(body);
        final Map<String, String> headers = (Map<String, String>) convertJsonNodeToObject(queueNode.get("headers"));
        if(headers != null) {
            headers.entrySet().forEach(e -> builder.withHeader(e.getKey(), e.getValue()));
        }
        return builder.build();
    }

    // TODO: Handle null here
    private BodyContent getBodyContent(JsonNode bodyNode) {
        final Object object = convertJsonNodeToObject(bodyNode);
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