package com.exemple.ecommerce.api.core.authorization;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.ecommerce.api.common.model.ApplicationBeanParam;
import com.exemple.ecommerce.api.common.security.ApiSecurityContext;

@Priority(Priorities.AUTHENTICATION)
public class AuthorizationFilter implements ContainerRequestFilter {

    @Autowired
    private AuthorizationContextService service;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        try {

            ApiSecurityContext context = service.buildContext(requestContext.getHeaderString("Authorization"));

            if (requestContext.getHeaders().containsKey(ApplicationBeanParam.APP_HEADER)
                    && !context.isApplicationInAud(requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER))) {

                throw new AuthorizationException(requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER) + " is not authorized");

            }

            requestContext.setSecurityContext(context);

        } catch (AuthorizationException e) {

            requestContext.abortWith(build(e));
        }

    }

    private static Response build(AuthorizationException e) {

        return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
    }

}
