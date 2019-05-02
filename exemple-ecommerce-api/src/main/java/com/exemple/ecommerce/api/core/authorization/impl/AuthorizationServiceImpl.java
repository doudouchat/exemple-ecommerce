package com.exemple.ecommerce.api.core.authorization.impl;

import java.util.logging.Level;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.exemple.ecommerce.api.core.authorization.AuthorizationService;

@Service
@Profile("!noSecurity")
public class AuthorizationServiceImpl implements AuthorizationService {

    @Value("${api.authorization.path}")
    private String path;

    @Value("${api.authorization.connectionTimeout:3000}")
    private int connectionTimeout;

    @Value("${api.authorization.socketTimeout:3000}")
    private int socketTimeout;

    private final Client client;

    public AuthorizationServiceImpl() {

        client = ClientBuilder.newClient()

                // timeout

                .property(ClientProperties.CONNECT_TIMEOUT, connectionTimeout)

                .property(ClientProperties.READ_TIMEOUT, socketTimeout)

                // authentification

                .register(HttpAuthenticationFeature.basicBuilder().build())

                // logging

                .register(LoggingFeature.class)

                .property(LoggingFeature.LOGGING_FEATURE_VERBOSITY, LoggingFeature.Verbosity.PAYLOAD_ANY)

                .property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL, Level.FINE.getName());

    }

    @Override
    public Response checkToken(String token, String username, String password) {

        Form form = new Form();
        form.param("token", token);

        return client.target(path + "/oauth/check_token").request()

                .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, username)
                .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

    }

}
