package com.exemple.ecommerce.customer.login.impl;

import org.springframework.stereotype.Service;

import com.exemple.ecommerce.customer.login.LoginService;
import com.exemple.ecommerce.resource.login.LoginResource;

@Service
public class LoginServiceImpl implements LoginService {

    private LoginResource loginResource;

    public LoginServiceImpl(LoginResource loginResource) {
        this.loginResource = loginResource;
    }

    @Override
    public boolean exist(String login) {
        return loginResource.get(login).isPresent();
    }
}
