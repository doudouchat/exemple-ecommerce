package com.exemple.ecommerce.api.common.security;

import java.security.Principal;
import java.util.Collection;
import java.util.function.Predicate;

import javax.ws.rs.core.SecurityContext;

public class ApiSecurityContext implements SecurityContext {

    private final Principal principal;

    private final String scheme;

    private final Predicate<String> containsRole;

    private ApiSecurityContext(Principal principal, String scheme, Predicate<String> containsRole) {
        this.principal = principal;
        this.scheme = scheme;
        this.containsRole = containsRole;
    }

    public ApiSecurityContext(Principal principal, String scheme, Collection<String> roles) {
        this(principal, scheme, roles::contains);
    }

    public ApiSecurityContext(Principal principal, String scheme) {
        this(principal, scheme, (String role) -> true);
    }

    @Override
    public Principal getUserPrincipal() {
        return this.principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return containsRole.test(role);
    }

    @Override
    public boolean isSecure() {
        return "https".equals(this.scheme);
    }

    @Override
    public String getAuthenticationScheme() {
        return SecurityContext.BASIC_AUTH;
    }

}
