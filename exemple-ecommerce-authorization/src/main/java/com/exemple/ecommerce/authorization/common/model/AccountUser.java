package com.exemple.ecommerce.authorization.common.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.Assert;

public class AccountUser extends User {

    private static final long serialVersionUID = 1L;

    private final UUID account;

    private final Set<String> scopes;

    public AccountUser(UUID account, String username, String password, Collection<? extends GrantedAuthority> authorities, Set<String> scopes) {
        super(username, password, authorities);
        Assert.notNull(account, "account is required");
        Assert.notNull(scopes, "scopes is required");
        this.account = account;
        this.scopes = Collections.unmodifiableSet(scopes);
    }

    public UUID getAccount() {
        return account;
    }

    public Set<String> getScopes() {
        return scopes;
    }

}
