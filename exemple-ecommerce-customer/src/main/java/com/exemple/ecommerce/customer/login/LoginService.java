package com.exemple.ecommerce.customer.login;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.ecommerce.customer.login.exception.LoginServiceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;

public interface LoginService {

    boolean exist(String login);

    void save(@NotBlank String login, @NotNull JsonNode source, @NotBlank String app, @NotBlank String version);

    JsonNode get(@NotBlank String login, @NotBlank String app, @NotBlank String version) throws LoginServiceNotFoundException;
}
