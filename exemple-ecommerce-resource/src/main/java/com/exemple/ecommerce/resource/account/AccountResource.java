package com.exemple.ecommerce.resource.account;

import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.exemple.ecommerce.resource.core.statement.AccountStatement;
import com.exemple.ecommerce.resource.core.validator.Json;
import com.fasterxml.jackson.databind.JsonNode;

public interface AccountResource {

    JsonNode save(@NotNull UUID id, @NotNull @Json(table = AccountStatement.TABLE) JsonNode account);

    JsonNode update(@NotNull UUID id, @NotNull @Json(table = AccountStatement.TABLE) JsonNode account);

    Optional<JsonNode> get(UUID id);

    JsonNode getByStatus(String status);

}
