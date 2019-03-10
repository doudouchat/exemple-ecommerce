package com.exemple.ecommerce.resource.login;

import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.ecommerce.resource.core.validator.LoginTable;
import com.exemple.ecommerce.resource.login.exception.LoginResourceException;
import com.fasterxml.jackson.databind.JsonNode;

public interface LoginResource {

    void save(@NotNull UUID id, @LoginTable JsonNode source) throws LoginResourceException;

    void update(@NotNull UUID id, @LoginTable JsonNode source) throws LoginResourceException;

    Optional<JsonNode> get(@NotBlank String login);
}
