package com.exemple.ecommerce.api.common.security;

import java.security.Principal;
import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import javax.ws.rs.core.SecurityContext;

public class ApiSecurityContext implements SecurityContext {

    private final Principal principal;

    private final String scheme;

    private final Predicate<String> containsRole;

    private final Predicate<String> containsApplication;

    private final BiPredicate<String, Resource> isAuthorizedPredicate;

    private ApiSecurityContext(Principal principal, String scheme, Predicate<String> containsRole, BiPredicate<String, Resource> isAuthorized,
            Predicate<String> containsApplication) {
        this.principal = principal;
        this.scheme = scheme;
        this.containsRole = containsRole;
        this.isAuthorizedPredicate = isAuthorized;
        this.containsApplication = containsApplication;
    }

    public ApiSecurityContext(Principal principal, String scheme, Collection<String> roles, Collection<String> applications) {
        this(principal, scheme, roles::contains, (String identity, Resource resource) -> principal.getName().equals(identity),
                applications::contains);
    }

    public ApiSecurityContext(Principal principal, String scheme) {
        this(principal, scheme, (String role) -> true, (String identity, Resource resource) -> true, (String app) -> true);
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

    public boolean isAuthorized(String identity, Resource resource) {
        return this.isAuthorizedPredicate.test(identity, resource);
    }

    public boolean isApplicationInAud(String application) {
        return containsApplication.test(application);
    }

    public enum Resource {

        ACCOUNT, LOGIN
    }

}
