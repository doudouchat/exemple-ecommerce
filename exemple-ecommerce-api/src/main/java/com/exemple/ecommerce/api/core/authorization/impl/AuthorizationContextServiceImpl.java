package com.exemple.ecommerce.api.core.authorization.impl;

import java.security.Principal;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.auth0.jwt.impl.JWTParser;
import com.auth0.jwt.interfaces.JWTPartsParser;
import com.auth0.jwt.interfaces.Payload;
import com.exemple.ecommerce.api.common.security.ApiSecurityContext;
import com.exemple.ecommerce.api.core.authorization.AuthorizationContextService;
import com.exemple.ecommerce.api.core.authorization.AuthorizationException;
import com.exemple.ecommerce.api.core.authorization.AuthorizationService;

@Service
@Profile("!noSecurity")
public class AuthorizationContextServiceImpl implements AuthorizationContextService {

    private static final Pattern BEARER;

    private AuthorizationService authorizationService;

    public AuthorizationContextServiceImpl(AuthorizationService authorizationService) {

        this.authorizationService = authorizationService;

    }

    static {

        BEARER = Pattern.compile("Bearer ");
    }

    @Override
    public ApiSecurityContext buildContext(String token) throws AuthorizationException {

        if (token != null) {

            Response response = this.authorizationService.checkToken(BEARER.matcher(token).replaceFirst(""), "resource", "secret");

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {

                JWTPartsParser parser = new JWTParser();
                Payload payload = parser.parsePayload(response.readEntity(String.class));

                Principal principal = () -> payload.getClaims().getOrDefault("id", payload.getClaim("client_id")).asString();

                return new ApiSecurityContext(principal, "https",
                        payload.getClaim("authorities").asList(String.class).stream()
                                .flatMap(role -> Stream.concat(Stream.of(role), payload.getClaim("scope").asList(String.class).stream()))
                                .collect(Collectors.toList()),
                        payload.getAudience());

            }

            throw new AuthorizationException();

        }

        return new ApiSecurityContext(() -> "anonymous", "http", Collections.emptyList(), Collections.emptyList());
    }

}
