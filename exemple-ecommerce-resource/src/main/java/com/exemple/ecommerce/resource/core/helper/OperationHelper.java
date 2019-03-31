package com.exemple.ecommerce.resource.core.helper;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import com.datastax.driver.core.AbstractTableMetadata;
import com.datastax.driver.core.DataType;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

public final class OperationHelper {

    private static BinaryOperator<JsonNode> function = (n1, n2) -> n2;

    private OperationHelper() {

    }

    public static JsonNode diff(JsonNode source, JsonNode target, AbstractTableMetadata tableMetadata) {

        return JsonNodeUtils.stream(source.fields())

                .map((Map.Entry<String, JsonNode> node) -> diffTransform(target, tableMetadata, node))
                // filter
                .filter(Objects::nonNull)
                .filter(node -> !Objects.equals(target.has(node.getKey()), target.get(node.getKey()).equals(node.getValue())))
                .reduce(JsonNodeUtils.init(), (JsonNode root, Map.Entry<String, ?> node) -> {
                    JsonNodeUtils.set(root, node.getValue(), node.getKey());
                    return root;
                }, function);

    }

    private static Map.Entry<String, ?> diffTransform(JsonNode target, AbstractTableMetadata tableMetadata, Map.Entry<String, JsonNode> node) {

        Entry<String, ?> result = node;

        if (DataType.Name.MAP == tableMetadata.getColumn(node.getKey()).getType().getName() && JsonNodeType.NULL != node.getValue().getNodeType()) {

            JsonNode item = target.get(node.getKey());

            JsonNode jsonNode = JsonNodeUtils.stream(node.getValue().fields())
                    .filter(e -> (item.get(e.getKey()) == null && e.getValue().getNodeType() != JsonNodeType.NULL)
                            || (item.get(e.getKey()) != null && !item.get(e.getKey()).equals(e.getValue())))
                    .reduce(JsonNodeUtils.init(), (JsonNode root, Map.Entry<String, JsonNode> e) -> {
                        JsonNodeUtils.set(root, e.getValue(), e.getKey());
                        return root;
                    }, function);

            result = null;
            if (jsonNode.elements().hasNext()) {
                result = Collections.singletonMap(node.getKey(), jsonNode).entrySet().iterator().next();

            }

        }

        if (DataType.Name.SET == tableMetadata.getColumn(node.getKey()).getType().getName()) {

            Set<JsonNode> jsonNodes = JsonNodeUtils.stream(node.getValue().elements())
                    .filter(e -> JsonNodeUtils.stream(target.get(node.getKey()).elements()).noneMatch(e::equals)).collect(Collectors.toSet());

            result = null;
            if (!jsonNodes.isEmpty()) {
                result = Collections.singletonMap(node.getKey(), jsonNodes).entrySet().iterator().next();

            }

        }

        return result;
    }

}
