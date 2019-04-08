package com.exemple.ecommerce.resource.account;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.exemple.ecommerce.resource.account.exception.AccountLoginResourceException;
import com.exemple.ecommerce.resource.core.validator.LoginTable;
import com.fasterxml.jackson.databind.JsonNode;

public interface AccountLoginResource {

    void save(@NotNull UUID id, @LoginTable JsonNode source) throws AccountLoginResourceException;

    void update(@NotNull UUID id, @LoginTable JsonNode source) throws AccountLoginResourceException;
}
