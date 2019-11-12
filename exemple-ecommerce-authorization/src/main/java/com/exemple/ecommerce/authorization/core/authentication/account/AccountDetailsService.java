package com.exemple.ecommerce.authorization.core.authentication.account;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.exemple.ecommerce.resource.common.util.JsonNodeUtils;
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
        boolean disabled = login.path("disabled").asBoolean(false);
        boolean accountLocked = login.path("accountLocked").asBoolean(false);

        List<SimpleGrantedAuthority> roles = JsonNodeType.MISSING == login.findPath("roles").getNodeType() ? Collections.emptyList()
                : JsonNodeUtils.stream(login.get("roles").elements()).map(JsonNode::asText).map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        return new User(username, password, !disabled, true, true, !accountLocked, roles);
    }
}
