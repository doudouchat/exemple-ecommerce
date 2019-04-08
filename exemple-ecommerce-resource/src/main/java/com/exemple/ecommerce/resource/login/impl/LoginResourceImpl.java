package com.exemple.ecommerce.resource.login.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.exemple.ecommerce.resource.core.statement.LoginStatement;
import com.exemple.ecommerce.resource.login.LoginResource;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Validated
public class LoginResourceImpl implements LoginResource {

    private static final Logger LOG = LoggerFactory.getLogger(LoginResourceImpl.class);

    private LoginStatement loginStatement;

    public LoginResourceImpl(LoginStatement loginStatement) {
        this.loginStatement = loginStatement;
    }

    @Override
    public Optional<JsonNode> get(String login) {

        JsonNode source = loginStatement.get(login);

        LOG.debug("get login {} {}", login, source);

        return Optional.ofNullable(source);
    }

}
