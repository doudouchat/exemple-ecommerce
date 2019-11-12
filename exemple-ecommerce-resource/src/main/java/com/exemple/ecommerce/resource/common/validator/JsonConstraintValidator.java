package com.exemple.ecommerce.resource.common.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.core.type.DataType;
import com.exemple.ecommerce.resource.common.JsonValidator;
import com.exemple.ecommerce.resource.common.JsonValidatorException;
import com.exemple.ecommerce.resource.common.util.JsonNodeUtils;
import com.exemple.ecommerce.resource.common.util.MetadataSchemaUtils;
import com.fasterxml.jackson.databind.JsonNode;

public class JsonConstraintValidator implements ConstraintValidator<Json, JsonNode> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonConstraintValidator.class);

    private static final String UNKNOWN = "UNKNOWN";

    private String table;

    private String messageTemplate;

    private final JsonValidator jsonValidator;

    private final CqlSession session;

    public JsonConstraintValidator(CqlSession session, JsonValidator jsonValidator) {
        this.session = session;
        this.jsonValidator = jsonValidator;
    }

    @Override
    public boolean isValid(JsonNode source, ConstraintValidatorContext context) {

        boolean valid = true;

        if (source != null) {

            List<JsonValidatorException> exceptions = new ArrayList<>(1);

            JsonNodeUtils.stream(source.fields()).allMatch((Map.Entry<String, JsonNode> node) -> {

                String key = node.getKey();

                TableMetadata tableMetadata = MetadataSchemaUtils.getTableMetadata(session, table);
                Optional<ColumnMetadata> column = tableMetadata.getColumn(key);

                if (!column.isPresent()) {
                    exceptions.add(new JsonValidatorException(UNKNOWN, key));
                    return false;
                }

                DataType type = tableMetadata.getColumn(key).get().getType();
                try {
                    LOG.trace("field {} type {} value {} column type {}", key, node.getValue().getNodeType(), node.getValue(),
                            type.asCql(false, true));
                    this.jsonValidator.valid(type, key, node.getValue());
                } catch (JsonValidatorException e) {
                    exceptions.add(e);
                    return false;
                }

                return true;
            });

            if (!exceptions.isEmpty()) {

                JsonValidatorException exception = exceptions.get(0);

                valid = false;

                LOG.trace(exception.getMessage(messageTemplate), exception);

                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(exception.getMessage(this.messageTemplate)).addPropertyNode(exception.getNode())
                        .addConstraintViolation();
            }

        }

        return valid;
    }

    @Override
    public void initialize(Json constraintAnnotation) {

        this.table = constraintAnnotation.table();
        this.messageTemplate = constraintAnnotation.message();

    }

}
