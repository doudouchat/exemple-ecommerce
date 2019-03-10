package com.exemple.ecommerce.api.core;

import java.util.logging.Level;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.ext.ContextResolver;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.server.validation.ValidationConfig;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.MessageSourceResourceBundleLocator;

import com.exemple.ecommerce.api.core.authorization.AuthorizationFilter;
import com.exemple.ecommerce.api.core.filter.CorsResponseFilter;
import com.exemple.ecommerce.api.core.filter.ExcutionContextResponseFilter;
import com.exemple.ecommerce.api.core.keyspace.KeyspaceFilter;
import com.exemple.ecommerce.api.core.listener.ApiEventListener;
import com.exemple.ecommerce.api.core.swagger.DocumentApiResource;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

@ApplicationPath("/ws")
public class ApiJerseyConfiguration extends ResourceConfig {

    public ApiJerseyConfiguration() {

        // Resources
        packages(
                // exception provider
                "com.exemple.ecommerce.api.core.exception",
                // actuate
                "com.exemple.ecommerce.api.core.actuate",
                // account
                "com.exemple.ecommerce.api.account",
                // connexion
                "com.exemple.ecommerce.api.connexion",
                // schema
                "com.exemple.ecommerce.api.schema",
                // login
                "com.exemple.ecommerce.api.login",
                // stock
                "com.exemple.ecommerce.api.stock")

                        // Nom de l'application
                        .setApplicationName("WS Ecommerce")

                        // validation
                        .register(ValidationConfigurationContextResolver.class)

                        // CORS response

                        .register(CorsResponseFilter.class)

                        // swagger

                        .register(new DocumentApiResource())

                        // execution context

                        .register(ExcutionContextResponseFilter.class)

                        // listener event

                        .register(ApiEventListener.class)

                        // security

                        .register(RolesAllowedDynamicFeature.class)

                        .register(AuthorizationFilter.class)

                        // keyspace

                        .register(KeyspaceFilter.class)

                        // logging

                        .register(LoggingFeature.class)

                        .property(LoggingFeature.LOGGING_FEATURE_VERBOSITY, LoggingFeature.Verbosity.PAYLOAD_ANY)

                        .property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL, Level.FINE.getName())

                        // JSON
                        .register(JacksonJsonProvider.class);

    }

    protected static class ValidationConfigurationContextResolver implements ContextResolver<ValidationConfig> {

        @Override
        public ValidationConfig getContext(Class<?> type) {

            ValidationConfig config = new ValidationConfig();

            ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

            messageSource.setCacheSeconds(0);
            messageSource.setBasename("classpath:messages/erreur_messages");

            config.messageInterpolator(new ResourceBundleMessageInterpolator(new MessageSourceResourceBundleLocator(messageSource)));

            return config;
        }

    }

}
