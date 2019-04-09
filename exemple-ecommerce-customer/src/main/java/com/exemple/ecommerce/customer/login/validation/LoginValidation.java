package com.exemple.ecommerce.customer.login.validation;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.JsonNode;

public interface LoginValidation {

    void validate(@NotNull JsonNode form, JsonNode old);

}
