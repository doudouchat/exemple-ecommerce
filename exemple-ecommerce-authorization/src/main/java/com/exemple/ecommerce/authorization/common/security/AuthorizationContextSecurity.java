package com.exemple.ecommerce.authorization.common.security;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

public class AuthorizationContextSecurity implements SecurityContext {

    private final OAuth2Authentication authentication;

    public AuthorizationContextSecurity(OAuth2Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public Principal getUserPrincipal() {
        return () -> authentication.getPrincipal().toString();
    }

    @Override
    public boolean isUserInRole(String role) {
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch((String authority) -> authority.equals(role));
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return SecurityContext.BASIC_AUTH;
    }

    public OAuth2Authentication getAuthentication() {
        return authentication;
    }
}
