package com.exemple.ecommerce.customer.login.impl;

import org.springframework.stereotype.Service;

import com.exemple.ecommerce.customer.login.LoginService;
import com.exemple.ecommerce.customer.login.exception.LoginServiceException;
import com.exemple.ecommerce.customer.login.exception.LoginServiceExistException;
import com.exemple.ecommerce.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.ecommerce.customer.login.validation.LoginValidation;
import com.exemple.ecommerce.resource.login.LoginResource;
import com.exemple.ecommerce.resource.login.exception.LoginResourceExistException;
import com.exemple.ecommerce.schema.filter.SchemaFilter;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class LoginServiceImpl implements LoginService {

    private LoginResource loginResource;

    private LoginValidation loginValidation;

    private SchemaFilter schemaFilter;

    public LoginServiceImpl(LoginResource loginResource, LoginValidation loginValidation, SchemaFilter schemaFilter) {
        this.loginResource = loginResource;
        this.loginValidation = loginValidation;
        this.schemaFilter = schemaFilter;
    }

    @Override
    public boolean exist(String login) {
        return loginResource.get(login).isPresent();
    }

    @Override
    public void save(String login, JsonNode source, String app, String version) throws LoginServiceNotFoundException {

        JsonNode old = loginResource.get(login).orElseThrow(LoginServiceNotFoundException::new);

        loginValidation.validate(source, old, app, version);

        loginResource.save(login, source);

    }

    @Override
    public void save(JsonNode source, String app, String version) throws LoginServiceException {

        loginValidation.validate(source, null, app, version);

        try {
            loginResource.save(source);
        } catch (LoginResourceExistException e) {
            throw new LoginServiceExistException(e);
        }

    }

    @Override
    public void delete(String login) {

        loginResource.delete(login);

    }

    @Override
    public JsonNode get(String login, String app, String version) throws LoginServiceNotFoundException {

        JsonNode source = loginResource.get(login).orElseThrow(LoginServiceNotFoundException::new);

        return schemaFilter.filter(app, version, "login", source);
    }

}
