package com.exemple.ecommerce.customer.account;

import java.util.UUID;

import com.exemple.ecommerce.customer.account.exception.AccountServiceException;
import com.fasterxml.jackson.databind.JsonNode;

public interface AccountService {

    JsonNode save(JsonNode account) throws AccountServiceException;

    JsonNode save(UUID id, JsonNode account) throws AccountServiceException;

    JsonNode get(UUID id) throws AccountServiceException;

}
