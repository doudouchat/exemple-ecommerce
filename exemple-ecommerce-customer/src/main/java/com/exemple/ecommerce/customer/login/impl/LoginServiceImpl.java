package com.exemple.ecommerce.customer.login.impl;

import org.springframework.stereotype.Service;

import com.exemple.ecommerce.customer.login.LoginService;
import com.exemple.ecommerce.customer.login.validation.LoginValidation;
import com.exemple.ecommerce.resource.login.LoginResource;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class LoginServiceImpl implements LoginService {

    private LoginResource loginResource;

    private LoginValidation loginValidation;

    public LoginServiceImpl(LoginResource loginResource, LoginValidation loginValidation) {
        this.loginResource = loginResource;
        this.loginValidation = loginValidation;
    }

    @Override
    public boolean exist(String login) {
        return loginResource.get(login).isPresent();
    }

    @Override
    public void save(String login, JsonNode source) {
        
        loginValidation.validate(source, null);

        loginResource.save(login, source);

    }
}
