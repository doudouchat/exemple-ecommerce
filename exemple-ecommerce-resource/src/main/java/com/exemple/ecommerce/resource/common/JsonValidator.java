package com.exemple.ecommerce.resource.common;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.ListType;
import com.datastax.oss.driver.api.core.type.MapType;
import com.datastax.oss.driver.api.core.type.SetType;
import com.datastax.oss.driver.api.core.type.TupleType;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.exemple.ecommerce.resource.common.util.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

@Component
public class JsonValidator {

    private static final Set<JsonNodeType> EXCLUDE_TYPES = EnumSet.of(JsonNodeType.NULL, JsonNodeType.MISSING);

    private static final String UNKNOWN = "UNKNOWN";

    private final CqlSession session;

    public JsonValidator(CqlSession session) {
        this.session = session;
    }

    public void valid(DataType dataType, String key, JsonNode value) throws JsonValidatorException {

        if (!EXCLUDE_TYPES.contains(value.getNodeType())) {

            if (dataType instanceof MapType) {

                this.valid((MapType) dataType, key, value);

            } else if (dataType instanceof SetType) {

                this.valid((SetType) dataType, key, value);

            } else if (dataType instanceof ListType) {

                this.valid((ListType) dataType, key, value);

            } else if (dataType instanceof UserDefinedType) {

                this.valid((UserDefinedType) dataType, value);

            } else if (dataType instanceof TupleType) {

                this.valid((TupleType) dataType, key, value);

            } else {

                valid(dataType, value.getNodeType(), value.asText(), key);
            }

        }

    }

    private void valid(MapType dataType, String key, JsonNode value) throws JsonValidatorException {

        validNodeType(value, JsonNodeType.OBJECT, key);

        List<JsonValidatorException> exceptions = new ArrayList<>(1);

        JsonNodeUtils.stream(value.fields()).allMatch((Entry<String, JsonNode> node) -> {
            DataType keyType = dataType.getKeyType();
            DataType valueType = dataType.getValueType();

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

    private void valid(DataType dataType, JsonNodeType type, Object value, String node) throws JsonValidatorException {

        TypeCodec<Object> typeCodec = session.getContext().getCodecRegistry().codecFor(dataType);

        Class<Object> javaType = typeCodec.getJavaType().getRawType();

        try {

            String valueString = String.valueOf(value);

            if (javaType.equals(String.class) || javaType.equals(java.time.Instant.class)) {

                valueString = "'" + valueString + "'";

                if (JsonNodeType.STRING != type) {
                    throw new JsonValidatorException(dataType.asCql(false, true), node);
                }

            }

            typeCodec.parse(valueString);

        } catch (IllegalArgumentException e) {
            throw new JsonValidatorException(dataType.asCql(false, true), node, e);
        }
    }

    private void valid(ListType dataType, String key, JsonNode value) throws JsonValidatorException {

        validElementType(dataType.getElementType(), key, value);
    }

    private void valid(SetType dataType, String key, JsonNode value) throws JsonValidatorException {

        validElementType(dataType.getElementType(), key, value);

    }

    private void valid(UserDefinedType dataType, JsonNode value) throws JsonValidatorException {

        List<JsonValidatorException> exceptions = new ArrayList<>(1);

        JsonNodeUtils.stream(value.fields()).allMatch((Entry<String, JsonNode> node) -> {

            if (!dataType.contains(node.getKey())) {
                exceptions.add(new JsonValidatorException(UNKNOWN, node.getKey()));
                return false;
            }
            DataType type = dataType.getFieldTypes().get(dataType.firstIndexOf(node.getKey()));
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

    private void valid(TupleType dataType, String key, JsonNode value) throws JsonValidatorException {

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

    private void validElementType(DataType type, String key, JsonNode value) throws JsonValidatorException {

        validNodeType(value, JsonNodeType.ARRAY, key);

        List<JsonValidatorException> exceptions = new ArrayList<>(1);

        StreamSupport.stream(value.spliterator(), false).allMatch((JsonNode node) -> {
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

    private static void validNodeType(JsonNode value, JsonNodeType nodeType, String node) throws JsonValidatorException {

        if (nodeType != value.getNodeType()) {
            throw new JsonValidatorException(nodeType.toString(), node);
        }
    }

}
