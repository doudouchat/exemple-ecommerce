package com.exemple.ecommerce.resource.core.statement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

public class StatementResource {

    private static final Logger LOG = LoggerFactory.getLogger(StatementResource.class);

    private final String table;

    private final Cluster cluster;

    protected StatementResource(Cluster cluster, String table) {
        this.table = table;
        this.cluster = cluster;
    }

    public TableMetadata getTableMetadata() {
        return cluster.getMetadata().getKeyspace(ResourceExecutionContext.get().keyspace()).getTable(this.table);
    }

    public Insert insert(JsonNode source) {

        Insert insert = QueryBuilder.insertInto(ResourceExecutionContext.get().keyspace(), this.table);

        insert.json(source);

        return insert;
    }

    public Update update(JsonNode source) {

        TableMetadata tableMetadata = getTableMetadata();

        Update update = QueryBuilder.update(ResourceExecutionContext.get().keyspace(), tableMetadata.getName());

        source.fields().forEachRemaining((Map.Entry<String, JsonNode> node) -> {

            DataType type = tableMetadata.getColumn(node.getKey()).getType();

            LOG.trace("{} column:{} type:{} value:{}", tableMetadata.getName(), node.getKey(), type.getName(), node.getValue());

            switch (type.getName()) {
                case MAP:

                    updateMap(update, node);

                    break;
                case SET:

                    updateSet(update, node);

                    break;
                default:
                    update.with(QueryBuilder.set(node.getKey(), getValue(node.getValue())));
            }

        });

        return update;
    }

    private static void updateMap(Update update, Map.Entry<String, JsonNode> node) {

        if (JsonNodeType.NULL == node.getValue().getNodeType()) {

            update.with(QueryBuilder.set(node.getKey(), getValue(node.getValue())));

        } else {

            Map<Object, Object> putValues = new HashMap<>();
            Set<Object> deleteValues = new HashSet<>();

            node.getValue().fields().forEachRemaining((Map.Entry<String, JsonNode> e) -> {

                if (!e.getValue().isNull()) {

                    putValues.put(getValue(JsonNodeUtils.create(e.getKey())), getValue(e.getValue()));

                } else {

                    deleteValues.add(getValue(JsonNodeUtils.create(e.getKey())));
                }

            });

            update.with(QueryBuilder.putAll(node.getKey(), putValues));
            update.with(QueryBuilder.removeAll(node.getKey(), deleteValues));

        }

    }

    private static void updateSet(Update update, Map.Entry<String, JsonNode> node) {

        Set<?> values = JsonNodeUtils.stream(node.getValue().elements()).map(StatementResource::getValue).collect(Collectors.toSet());

        update.with(QueryBuilder.addAll(node.getKey(), values));

    }

    private static Object getValue(JsonNode source) {

        Object value = null;

        if (!source.isNull()) {

            value = QueryBuilder.fromJson(source);
        }

        return value;
    }
}
