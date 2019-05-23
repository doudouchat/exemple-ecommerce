package com.exemple.ecommerce.authorization.core.authentication.account;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.exemple.ecommerce.authorization.common.model.AccountUser;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.login.LoginResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

@Service
public class AccountDetailsService implements UserDetailsService {

    private final LoginResource loginResource;

    public AccountDetailsService(LoginResource loginResource) {

        this.loginResource = loginResource;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {

        JsonNode login = loginResource.get(username).orElseThrow(() -> new UsernameNotFoundException(username));

        String password = login.get("password").textValue();

        List<SimpleGrantedAuthority> roles = JsonNodeType.MISSING == login.findPath("roles").getNodeType() ? Collections.emptyList()
                : JsonNodeUtils.stream(login.get("roles").elements()).map(JsonNode::asText).map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        Set<String> scopes = JsonNodeType.MISSING == login.findPath("scopes").getNodeType() ? Collections.emptySet()
                : JsonNodeUtils.stream(login.get("scopes").elements()).map(JsonNode::asText).collect(Collectors.toSet());

        return new AccountUser(username, password, roles, scopes);
    }
}
