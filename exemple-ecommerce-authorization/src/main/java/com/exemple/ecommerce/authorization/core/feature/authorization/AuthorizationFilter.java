package com.exemple.ecommerce.authorization.core.feature.authorization;

import java.util.regex.Pattern;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;

import com.exemple.ecommerce.authorization.common.security.AuthorizationContextSecurity;
import com.exemple.ecommerce.authorization.core.feature.FeatureConfiguration;
import com.exemple.ecommerce.authorization.core.resource.keyspace.AuthorizationResourceKeyspace;

@Priority(Priorities.AUTHENTICATION)
public class AuthorizationFilter implements ContainerRequestFilter {

    private static final Pattern BEARER;

    static {

        BEARER = Pattern.compile("Bearer ");
    }

    @Autowired
    private DefaultTokenServices tokenServices;

    @Autowired
    private AuthorizationResourceKeyspace authorizationResourceKeyspace;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        if (StringUtils.startsWith(requestContext.getHeaderString("Authorization"), "Bearer ")) {

            try {

                OAuth2Authentication authentication = tokenServices
                        .loadAuthentication(BEARER.matcher(requestContext.getHeaderString("Authorization")).replaceFirst(""));

                if (!authentication.getOAuth2Request().getResourceIds().contains(requestContext.getHeaderString(FeatureConfiguration.APP_HEADER))) {

                    throw new InvalidTokenException(requestContext.getHeaderString(FeatureConfiguration.APP_HEADER) + " is not authorized");

                }

                requestContext.setSecurityContext(new AuthorizationContextSecurity(authentication));

                authorizationResourceKeyspace.initKeyspace(requestContext.getHeaderString(FeatureConfiguration.APP_HEADER));

            } catch (InvalidTokenException e) {

                requestContext.abortWith(build(e));
            }
        }

    }

    private static Response build(InvalidTokenException e) {

        return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
    }

}
