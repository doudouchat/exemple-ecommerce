package com.exemple.ecommerce.customer.login.impl;

import org.springframework.stereotype.Service;

import com.exemple.ecommerce.customer.login.LoginService;
import com.exemple.ecommerce.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.ecommerce.customer.login.validation.LoginValidation;
import com.exemple.ecommerce.resource.login.LoginResource;
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
    public void save(String login, JsonNode source, String app, String version) {

        loginValidation.validate(source, null, app, version);

        loginResource.save(login, source);

    }

    @Override
    public JsonNode get(String login, String app, String version) throws LoginServiceNotFoundException {

        JsonNode source = loginResource.get(login).orElseThrow(LoginServiceNotFoundException::new);

        return schemaFilter.filter(app, version, "login", source);
    }
}
