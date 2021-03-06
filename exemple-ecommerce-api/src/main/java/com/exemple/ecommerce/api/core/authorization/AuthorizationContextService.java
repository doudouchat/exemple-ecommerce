package com.exemple.ecommerce.api.core.authorization;

import java.util.UUID;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.exemple.ecommerce.api.common.security.ApiSecurityContext;

public interface AuthorizationContextService {

    ApiSecurityContext buildContext(MultivaluedMap<String, String> headers) throws AuthorizationException;

    void cleanContext(ApiSecurityContext securityContext, Response.StatusType statusInfo);

    void verifyAccountId(UUID id, ApiSecurityContext securityContext);

    void verifyLogin(String login, ApiSecurityContext securityContext);

}
