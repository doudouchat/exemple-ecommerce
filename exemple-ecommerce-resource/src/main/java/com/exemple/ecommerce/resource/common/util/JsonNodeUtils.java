package com.exemple.ecommerce.resource.common.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public static <T> Stream<T> stream(Iterator<T> source) {

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(source, Spliterator.ORDERED), false);
    }

}
