package com.exemple.ecommerce.api.core.authorization;

import com.exemple.ecommerce.api.common.security.ApiSecurityContext;

public interface AuthorizationContextService {

    ApiSecurityContext buildContext(String token) throws AuthorizationException;

}
