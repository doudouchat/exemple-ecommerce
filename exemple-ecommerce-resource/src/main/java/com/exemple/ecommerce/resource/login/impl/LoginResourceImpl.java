package com.exemple.ecommerce.resource.login.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Update;
import com.exemple.ecommerce.resource.account.AccountResource;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.core.statement.LoginStatement;
import com.exemple.ecommerce.resource.core.statement.ParameterStatement;
import com.exemple.ecommerce.resource.login.LoginResource;
import com.exemple.ecommerce.resource.login.exception.LoginResourceException;
import com.exemple.ecommerce.resource.login.exception.LoginResourceExistException;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Validated
public class LoginResourceImpl implements LoginResource {

    private static final Logger LOG = LoggerFactory.getLogger(LoginResourceImpl.class);

    private Session session;

    private LoginStatement loginStatement;

    private AccountResource accountResource;

    private ParameterStatement parameterStatement;

    public LoginResourceImpl(Session session, LoginStatement loginStatement, AccountResource accountResource, ParameterStatement parameterStatement) {
        this.session = session;
        this.loginStatement = loginStatement;
        this.accountResource = accountResource;
        this.parameterStatement = parameterStatement;
    }

    @Override
    public void save(UUID id, JsonNode source) throws LoginResourceExistException {

        LOG.debug("create login {} {}", id, source);

        Set<String> loginFields = parameterStatement.getLogins();

        JsonNode loginData = JsonNodeUtils.clone(source, loginFields.stream().toArray(String[]::new));

        save(id, id.toString(), loginData);
        save(loginFields.stream().filter(source::hasNonNull).map(field -> source.get(field).asText()), id, loginData);

    }

    private void save(UUID id, String login, JsonNode loginData) throws LoginResourceExistException {

        JsonNode loginNode = JsonNodeUtils.clone(loginData);
        JsonNodeUtils.set(loginNode, login, LoginStatement.LOGIN);
        JsonNodeUtils.set(loginNode, id, LoginStatement.ID);

        Insert insert = loginStatement.insert(loginNode);
        insert.ifNotExists();

        Row resultLogin = session.execute(insert).one();
        boolean notExistLogin = resultLogin.getBool(0);

        if (!notExistLogin) {
            throw new LoginResourceExistException(resultLogin.getString(1));
        }
    }

    private void save(Stream<String> stream, UUID id, JsonNode loginData) throws LoginResourceExistException {

        List<LoginResourceExistException> exceptions = new ArrayList<>(1);
        List<String> removeLogins = new ArrayList<>();
        stream.allMatch((String login) -> {

            LOG.debug("create login {}", login);

            try {
                save(id, login, loginData);
            } catch (LoginResourceExistException e) {
                exceptions.add(e);
                removeLogins.forEach(removeLogin -> this.session.execute(loginStatement.delete(removeLogin)));
                return false;
            }
            removeLogins.add(login);
            return true;

        });

        if (!exceptions.isEmpty()) {
            throw exceptions.get(0);
        }
    }

    @Override
    public void update(UUID id, JsonNode source) throws LoginResourceException {

        LOG.debug("update login {} {}", id, source);

        String idLogin = id.toString();
        JsonNode account = accountResource.get(id).orElseThrow(IllegalArgumentException::new);
        JsonNode loginData = JsonNodeUtils.clone(loginStatement.get(idLogin), LoginStatement.LOGIN);

        Set<String> loginFields = parameterStatement.getLogins();

        JsonNodeUtils.stream(source.fields()).filter(e -> !loginFields.contains(e.getKey()))
                .forEach(e -> JsonNodeUtils.set(loginData, e.getValue(), e.getKey()));

        Set<String> logins = new HashSet<>();
        Set<String> addlogins = new HashSet<>();
        Set<String> removeLogins = new HashSet<>();

        JsonNodeUtils.stream(source.fields()).filter(e -> loginFields.contains(e.getKey())).forEach((Map.Entry<String, JsonNode> e) -> {

            String field = e.getKey();
            String login = e.getValue().textValue();
            String oldLogin = account.has(field) ? account.get(field).textValue() : null;

            if (!Objects.equals(login, oldLogin)) {

                logins.add(field);

                if (login != null) {
                    addlogins.add(login);
                } else {
                    removeLogins.add(oldLogin);
                }

                if (oldLogin != null) {
                    removeLogins.add(oldLogin);
                }
            }
        });

        save(addlogins.stream(), id, loginData);
        removeLogins.forEach((String login) -> {
            LOG.debug("delete login {}", login);
            this.session.execute(loginStatement.delete(login));
        });

        this.session.execute(loginStatement.update(idLogin, loginData));

        loginFields.stream().filter(field -> !logins.contains(field)).filter(field -> account.get(field) != null).forEach((String e) -> {

            Update update = loginStatement.update(account.get(e).asText(), loginData);
            update.where().ifExists();

            Row resultLogin = this.session.execute(update).one();

            if (resultLogin.getBool(0)) {
                LOG.trace("update login {}", account.get(e).asText());
            }

        });

    }

    @Override
    public Optional<JsonNode> get(String login) {

        JsonNode source = loginStatement.get(login);

        LOG.debug("get login {} {}", login, source);

        return Optional.ofNullable(source);
    }

}
