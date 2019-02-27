package com.jonnymatts.jzonbie.jackson.body;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.*;
import com.jonnymatts.jzonbie.Body;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.jonnymatts.jzonbie.jackson.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.jackson.body.BodyContent.TYPE_IDENTIFIER;
import static com.jonnymatts.jzonbie.jackson.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.jackson.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.jackson.body.StringBodyContent.stringBody;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class BodyDeserializer extends StdDeserializer<Body> {

    public BodyDeserializer() {
        this(null);
    }

    public BodyDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Body deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        final JsonNode typeIdentifier = node.get(TYPE_IDENTIFIER);
        final JsonNode contentNode = node.get("content");

        final BodyContentType bodyContentType = BodyContentType.valueOf(typeIdentifier.textValue());
        final Object content = convertJsonNodeToObject(contentNode);
        switch (bodyContentType) {
            case J_OBJECT:
                return objectBody((Map<String, Object>)content);
            case J_ARRAY:
                return arrayBody((List<Object>)content);
            case J_STRING:
                return stringBody((String)content);
            default:
                return literalBody(content);
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