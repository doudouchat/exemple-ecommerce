package com.exemple.ecommerce.authorization.core.authentication.provider;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.exemple.ecommerce.authorization.common.model.AccountUser;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.login.LoginResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

@Component
public class AccountAuthenticationProvider extends DaoAuthenticationProvider {

    private final LoginResource loginResource;

    public AccountAuthenticationProvider(LoginResource loginResource) {

        this.loginResource = loginResource;
    }

    @PostConstruct
    protected void init() {

        this.setUserDetailsService((String username) -> {

            JsonNode login = loginResource.get(username).orElseThrow(() -> new UsernameNotFoundException(username));

            UUID account = UUID.fromString(login.get("id").textValue());
            String password = login.get("password").textValue();

            List<SimpleGrantedAuthority> roles = JsonNodeType.MISSING == login.findPath("roles").getNodeType() ? Collections.emptyList()
                    : JsonNodeUtils.stream(login.get("roles").elements()).map(JsonNode::asText).map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            Set<String> scopes = JsonNodeType.MISSING == login.findPath("scopes").getNodeType() ? Collections.emptySet()
                    : JsonNodeUtils.stream(login.get("scopes").elements()).map(JsonNode::asText).collect(Collectors.toSet());

            return new AccountUser(account, username, password, roles, scopes);
        });
    }

    @Override
    public boolean supports(Class<?> authentication) {

        return ((AbstractAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch("ROLE_APP"::equals);
    }

}
