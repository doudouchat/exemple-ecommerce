package com.exemple.ecommerce.schema.description;

import com.fasterxml.jackson.databind.JsonNode;

public interface SchemaDescription {

    JsonNode get(String app, String version, String resource);

    JsonNode getPatch();
}
