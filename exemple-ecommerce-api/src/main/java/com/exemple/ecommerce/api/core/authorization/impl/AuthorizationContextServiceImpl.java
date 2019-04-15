package com.exemple.ecommerce.api.core.authorization.impl;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
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
import com.exemple.ecommerce.api.core.ApiConfiguration;
import com.exemple.ecommerce.api.core.authorization.AuthorizationContextService;
import com.exemple.ecommerce.api.core.authorization.AuthorizationException;
import com.exemple.ecommerce.api.core.authorization.AuthorizationService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@Service
@Profile("!noSecurity")
public class AuthorizationContextServiceImpl implements AuthorizationContextService {

    private static final Pattern BEARER;

    private final AuthorizationService authorizationService;

    private final HazelcastInstance hazelcastInstance;

    private final JWTPartsParser parser;

    public AuthorizationContextServiceImpl(AuthorizationService authorizationService, HazelcastInstance hazelcastInstance) {

        this.authorizationService = authorizationService;
        this.hazelcastInstance = hazelcastInstance;
        this.parser = new JWTParser();

    }

    static {

        BEARER = Pattern.compile("Bearer ");
    }

    @Override
    public ApiSecurityContext buildContext(String token) throws AuthorizationException {

        if (token != null) {

            Payload payload = extractPayload(token);

            Principal principal = () -> payload.getClaims().getOrDefault("id", payload.getClaim("client_id")).asString();

            return new ApiSecurityContext(principal, "https",
                    payload.getClaim("authorities").asList(String.class).stream()
                            .flatMap(role -> Stream.concat(Stream.of(role), payload.getClaim("scope").asList(String.class).stream()))
                            .collect(Collectors.toList()),
                    payload.getAudience());

        }

        return new ApiSecurityContext(() -> "anonymous", "http", Collections.emptyList(), Collections.emptyList());
    }

    private Payload extractPayload(String token) throws AuthorizationException {

        Payload payload;
        IMap<String, String> tokens = hazelcastInstance.getMap(ApiConfiguration.AVAILABLE_TOKENS);
        if (!tokens.containsKey(token)) {

            Response response = this.authorizationService.checkToken(BEARER.matcher(token).replaceFirst(""), "resource", "secret");

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {

                throw new AuthorizationException();
            }

            String body = response.readEntity(String.class);
            payload = parser.parsePayload(body);
            if (payload.getExpiresAt() != null) {

                tokens.put(token, body, ChronoUnit.SECONDS.between(LocalDateTime.now(),
                        payload.getExpiresAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()), TimeUnit.SECONDS);
            }

        } else {

            payload = parser.parsePayload(tokens.get(token));
        }

        return payload;
    }

}
