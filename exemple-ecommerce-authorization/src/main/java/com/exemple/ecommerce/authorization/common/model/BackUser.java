package com.exemple.ecommerce.authorization.common.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.Assert;

public class BackUser extends User {

    private static final long serialVersionUID = 1L;

    private final Set<String> scopes;

    public BackUser(String username, String password, Collection<? extends GrantedAuthority> authorities, Set<String> scopes) {
        super(username, password, authorities);
        Assert.notNull(scopes, "scopes is required");
        this.scopes = Collections.unmodifiableSet(scopes);
    }

    public Set<String> getScopes() {
        return scopes;
    }

}
