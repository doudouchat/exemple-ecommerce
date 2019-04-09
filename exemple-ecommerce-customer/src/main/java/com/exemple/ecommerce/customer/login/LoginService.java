package com.exemple.ecommerce.customer.login;

import com.fasterxml.jackson.databind.JsonNode;

public interface LoginService {

    boolean exist(String login);

    void save(String login, JsonNode source);
}
