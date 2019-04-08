package com.exemple.ecommerce.resource.login;

import java.util.Optional;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.databind.JsonNode;

public interface LoginResource {

    Optional<JsonNode> get(@NotBlank String login);
}
