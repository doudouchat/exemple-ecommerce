package com.exemple.ecommerce.authorization.core.authentication.provider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.exemple.ecommerce.authorization.common.model.BackUser;

@Component
public class BackAuthenticationProvider extends DaoAuthenticationProvider {

    private Map<String, BackUser> users = new HashMap<>();

    public BackAuthenticationProvider() {

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        users.put("admin",
                new BackUser("admin", "{bcrypt}" + passwordEncoder.encode("admin123"),
                        Arrays.stream(new String[] { "ROLE_STOCK" }).map(SimpleGrantedAuthority::new).collect(Collectors.toList()),
                        Arrays.stream(new String[] { "stock:read", "stock:update" }).collect(Collectors.toSet())));
    }

    @PostConstruct
    protected void init() {

        this.setUserDetailsService((String username) -> {

            BackUser user = this.users.get(username);

            if (user == null) {

                throw new UsernameNotFoundException(username);
            }

            return user;
        });
    }

    @Override
    public boolean supports(Class<?> authentication) {

        return ((AbstractAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch("ROLE_BACK"::equals);
    }

}
