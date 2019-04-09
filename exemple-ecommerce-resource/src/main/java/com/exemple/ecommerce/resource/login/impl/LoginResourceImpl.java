package com.exemple.ecommerce.resource.login.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.driver.core.Session;
import com.exemple.ecommerce.resource.core.statement.LoginStatement;
import com.exemple.ecommerce.resource.login.LoginResource;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Validated
public class LoginResourceImpl implements LoginResource {

    private Session session;

    private LoginStatement loginStatement;

    public LoginResourceImpl(LoginStatement loginStatement, Session session) {
        this.loginStatement = loginStatement;
        this.session = session;
    }

    @Override
    public Optional<JsonNode> get(String login) {

        JsonNode source = loginStatement.get(login);

        return Optional.ofNullable(source);
    }

    @Override
    public void save(String login, JsonNode source) {

        JsonNode data = loginStatement.get(login);

        if (data == null) {

            session.execute(loginStatement.update(login, source));

        } else {

            loginStatement.findById(UUID.fromString(data.get(LoginStatement.ID).textValue())).stream()
                    .map((JsonNode l) -> l.get(LoginStatement.LOGIN).textValue())
                    .forEach((String l) -> session.execute(loginStatement.update(l, source)));
        }

    }

}
