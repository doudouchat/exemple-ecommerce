package com.exemple.ecommerce.resource.login;

import java.util.Optional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.ecommerce.resource.core.statement.LoginStatement;
import com.exemple.ecommerce.resource.core.validator.Json;
import com.fasterxml.jackson.databind.JsonNode;

public interface LoginResource {

    Optional<JsonNode> get(@NotBlank String login);

    void save(@NotBlank String login, @NotNull @Json(table = LoginStatement.LOGIN) JsonNode source);
}
