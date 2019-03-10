package com.exemple.ecommerce.resource.common;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class JsonNodeUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonNodeUtils() {

    }

    public static JsonNode clone(JsonNode resource, String... fields) {

        ObjectNode node = ((ObjectNode) resource).deepCopy();
        Arrays.stream(fields).forEach(node::remove);

        return node;
    }

    public static JsonNode init(String field) {

        ObjectNode node = (ObjectNode) init();
        node.set(field, null);

        return node;
    }

    public static JsonNode init() {

        return MAPPER.createObjectNode();
    }

    public static void set(JsonNode node, Object data, String field) {

        ((ObjectNode) node).set(field, create(data));
    }

    public static JsonNode create(Object data) {

        return MAPPER.convertValue(data, JsonNode.class);

    }

    public static void filter(JsonNode source) {

        filter(source, (Entry<String, JsonNode> e) -> {
            if (JsonNodeType.NULL == e.getValue().getNodeType()) {
                ((ObjectNode) source).remove(e.getKey());
            }

            if (JsonNodeType.OBJECT == e.getValue().getNodeType()) {

                filter(source.get(e.getKey()));
            }

            if (JsonNodeType.ARRAY == e.getValue().getNodeType()) {

                ((ObjectNode) source).replace(e.getKey(),
                        create(stream(e.getValue().elements()).filter(node -> JsonNodeType.NULL != node.getNodeType()).collect(Collectors.toList())));

                filter(source.get(e.getKey()));
            }
        });
    }

    public static void filter(JsonNode source, Consumer<Entry<String, JsonNode>> action) {

        if (source != null && JsonNodeType.OBJECT == source.getNodeType()) {
            JsonNodeUtils.stream(JsonNodeUtils.clone(source).fields()).forEach(action);
        }
    }

    public static <T> Stream<T> stream(Iterator<T> source) {

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(source, Spliterator.ORDERED), false);
    }

}
