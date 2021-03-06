package com.exemple.ecommerce.schema.validation;

import org.everit.json.schema.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public interface SchemaValidation {

    void validate(String app, String version, String resource, JsonNode form, JsonNode old);

    void validate(Schema schema, JsonNode target);

    void validatePatch(ArrayNode patch);

}
