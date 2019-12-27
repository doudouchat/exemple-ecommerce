package com.exemple.ecommerce.schema.core.validator;

import com.exemple.ecommerce.schema.common.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;

public interface ValidatorService {

    void validate(String path, JsonNode form, JsonNode old, ValidationException validationException);
}
