package com.exemple.ecommerce.api.core.authorization.impl;

import java.security.Principal;
import java.util.Collections;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.MultivaluedMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.interfaces.Payload;
import com.exemple.ecommerce.api.common.model.ApplicationBeanParam;
import com.exemple.ecommerce.api.common.security.ApiSecurityContext;
import com.exemple.ecommerce.api.core.authorization.AuthorizationAlgorithmFactory;
import com.exemple.ecommerce.api.core.authorization.AuthorizationContextService;
import com.exemple.ecommerce.api.core.authorization.AuthorizationException;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.core.statement.LoginStatement;
import com.exemple.ecommerce.resource.login.LoginResource;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Profile("!noSecurity")
public class AuthorizationContextServiceImpl implements AuthorizationContextService {

    private static final Pattern BEARER;

    private final AuthorizationAlgorithmFactory authorizationAlgorithmFactory;

    private final LoginResource loginResource;

    public AuthorizationContextServiceImpl(AuthorizationAlgorithmFactory authorizationAlgorithmFactory, LoginResource loginResource) {

        this.authorizationAlgorithmFactory = authorizationAlgorithmFactory;
        this.loginResource = loginResource;

    }

    static {

        BEARER = Pattern.compile("Bearer ");

    }

    @Override
    public ApiSecurityContext buildContext(MultivaluedMap<String, String> headers) throws AuthorizationException {

        String token = headers.getFirst("Authorization");

        if (token != null) {

            JWTVerifier verifier = JWT.require(authorizationAlgorithmFactory.getAlgorithm())
                    .withAudience(headers.getFirst(ApplicationBeanParam.APP_HEADER)).build();

            Payload payload;
            try {
                payload = verifier.verify(BEARER.matcher(token).replaceFirst(""));
            } catch (JWTVerificationException e) {
                throw new AuthorizationException(e);
            }

            Principal principal = () -> payload.getClaims().getOrDefault("user_name", payload.getClaim("client_id")).asString();

            return new ApiSecurityContext(principal, "https", payload.getClaim("scope").asList(String.class));

        }

        return new ApiSecurityContext(() -> "anonymous", "http", Collections.emptyList());
    }

    @Override
    public void verifyAccountId(UUID id, ApiSecurityContext securityContext) {

        JsonNode login = loginResource.get(securityContext.getUserPrincipal().getName()).orElseGet(JsonNodeUtils::init).get(LoginStatement.ID);

        if (!id.toString().equals(login.asText(null))) {

            throw new ForbiddenException();
        }

    }

    @Override
    public void verifyLogin(String login, ApiSecurityContext securityContext) {

        if (!login.equals(securityContext.getUserPrincipal().getName())) {

            throw new ForbiddenException();
        }

    }

}
