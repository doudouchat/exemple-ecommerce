package com.exemple.ecommerce.api.core.authorization.impl;

import java.security.Principal;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.exemple.ecommerce.api.common.security.ApiSecurityContext;
import com.exemple.ecommerce.api.core.authorization.AuthorizationContextService;

@Service
@Profile("noSecurity")
public class AuthorizationNoSecureContextServiceImpl implements AuthorizationContextService {

    @Override
    public ApiSecurityContext buildContext(String token) {

        Principal principal = () -> "anonymous";

        return new ApiSecurityContext(principal, "http");
    }

}
