package com.exemple.ecommerce.api.common.security;

import javax.ws.rs.ForbiddenException;

public final class ApiSecurityContextUtils {

    private ApiSecurityContextUtils() {

    }

    public static void checkAuthorization(String identity, ApiSecurityContext.Resource resource, ApiSecurityContext servletContext) {

        if (!servletContext.isAuthorized(identity, resource)) {

            throw new ForbiddenException();
        }

    }

}
