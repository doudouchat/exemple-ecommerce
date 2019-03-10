package com.exemple.ecommerce.resource.core.validator;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.TupleType;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UserType;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

@Component
public class JsonValidator {

    private static final Logger LOG = LoggerFactory.getLogger(JsonValidator.class);

    private static final Set<JsonNodeType> EXCLUDE_TYPES = EnumSet.of(JsonNodeType.NULL, JsonNodeType.MISSING);

    private static final String UNKNOWN = "UNKNOWN";

    private Cluster cluster;

    public JsonValidator(Cluster cluster) {
        this.cluster = cluster;
    }

    public void valid(JsonNode source, String table) throws JsonValidatorException {

        if (source != null) {

            List<JsonValidatorException> exceptions = new ArrayList<>(1);

            JsonNodeUtils.stream(source.fields()).allMatch((Map.Entry<String, JsonNode> node) -> {

                String key = node.getKey();

                TableMetadata tableMetadata = cluster.getMetadata().getKeyspace(ResourceExecutionContext.get().keyspace()).getTable(table);
                ColumnMetadata column = tableMetadata.getColumn(key);

                if (column == null) {
                    exceptions.add(new JsonValidatorException(UNKNOWN, key));
                    return false;
                }

                DataType type = tableMetadata.getColumn(key).getType();
                try {
                    valid(type, key, node.getValue());
                } catch (JsonValidatorException e) {
                    exceptions.add(e);
                    return false;
                }

                return true;
            });

            if (!exceptions.isEmpty()) {
                throw exceptions.get(0);
            }

        }
    }

    private void valid(DataType dataType, String key, JsonNode value) throws JsonValidatorException {

        LOG.trace("field {} type {} value {} column type {}", key, value.getNodeType(), value, dataType.getName());

        if (!EXCLUDE_TYPES.contains(value.getNodeType())) {

            switch (dataType.getName()) {

                case MAP:
                    this.validMap(dataType, key, value);
                    break;

                case SET:

                case LIST:
                    this.validList(dataType, key, value);
                    break;

                case UDT:
                    this.validUDT((UserType) dataType, value);
                    break;

                case TUPLE:
                    this.validTuple((TupleType) dataType, key, value);
                    break;

                default:

                    valid(dataType, value.getNodeType(), value.asText(), key);

            }
        }

    }

    private void validMap(DataType dataType, String key, JsonNode value) throws JsonValidatorException {

        validNodeType(value, JsonNodeType.OBJECT, key);

        List<JsonValidatorException> exceptions = new ArrayList<>(1);

        JsonNodeUtils.stream(value.fields()).allMatch((Entry<String, JsonNode> node) -> {
            DataType keyType = dataType.getTypeArguments().get(0);
            DataType valueType = dataType.getTypeArguments().get(1);

            try {
                valid(keyType, JsonNodeType.STRING, node.getKey(), node.getKey());
                valid(valueType, node.getKey(), node.getValue());
            } catch (JsonValidatorException e) {
                exceptions.add(e);
                return false;
            }

            return true;
        });

        if (!exceptions.isEmpty()) {
            throw exceptions.get(0);
        }
    }

    private void validList(DataType dataType, String key, JsonNode value) throws JsonValidatorException {

        validNodeType(value, JsonNodeType.ARRAY, key);

        List<JsonValidatorException> exceptions = new ArrayList<>(1);

        StreamSupport.stream(value.spliterator(), false).allMatch((JsonNode node) -> {
            DataType type = dataType.getTypeArguments().get(0);
            try {
                valid(type, key, node);
            } catch (JsonValidatorException e) {
                exceptions.add(e);
                return false;
            }

            return true;
        });

        if (!exceptions.isEmpty()) {
            throw exceptions.get(0);
        }
    }

    private void validUDT(UserType dataType, JsonNode value) throws JsonValidatorException {

        List<JsonValidatorException> exceptions = new ArrayList<>(1);

        JsonNodeUtils.stream(value.fields()).allMatch((Entry<String, JsonNode> node) -> {
            DataType type;
            try {
                type = dataType.getFieldType(node.getKey());
            } catch (IllegalArgumentException e) {
                exceptions.add(new JsonValidatorException(UNKNOWN, node.getKey(), e));
                return false;
            }
            try {
                valid(type, node.getKey(), node.getValue());
            } catch (JsonValidatorException e) {
                exceptions.add(e);
                return false;
            }
            return true;
        });

        if (!exceptions.isEmpty()) {
            throw exceptions.get(0);
        }
    }

    private void validTuple(TupleType dataType, String key, JsonNode value) throws JsonValidatorException {

        Iterator<JsonNode> fields = value.elements();
        Stream<DataType> types = dataType.getComponentTypes().stream();

        List<JsonValidatorException> exceptions = new ArrayList<>(1);

        types.allMatch((DataType type) -> {
            if (fields.hasNext()) {
                JsonNode node = fields.next();
                try {
                    valid(type, key, node);
                } catch (JsonValidatorException e) {
                    exceptions.add(e);
                    return false;
                }
                return true;
            }
            exceptions.add(new JsonValidatorException("MSSING", key));
            return false;
        });

        if (!exceptions.isEmpty()) {
            throw exceptions.get(0);
        }

        if (fields.hasNext()) {
            throw new JsonValidatorException(UNKNOWN, fields.next().asText());
        }
    }

    private static void validNodeType(JsonNode value, JsonNodeType nodeType, String node) throws JsonValidatorException {

        if (nodeType != value.getNodeType()) {
            throw new JsonValidatorException(nodeType.toString(), node);
        }
    }

    private void valid(DataType dataType, JsonNodeType type, Object value, String node) throws JsonValidatorException {

        TypeCodec<Object> typeCodec = cluster.getConfiguration().getCodecRegistry().codecFor(dataType);

        Class<Object> javaType = typeCodec.getJavaType().getRawType();

        try {

            String valueString = String.valueOf(value);

            if (javaType.equals(String.class)) {

                valueString = "'" + valueString + "'";

                if (JsonNodeType.STRING != type) {
                    throw new JsonValidatorException(dataType.getName().name(), node);
                }

            }

            typeCodec.parse(valueString);

        } catch (InvalidTypeException e) {
            throw new JsonValidatorException(dataType.getName().name(), node, e);
        }
    }

}
