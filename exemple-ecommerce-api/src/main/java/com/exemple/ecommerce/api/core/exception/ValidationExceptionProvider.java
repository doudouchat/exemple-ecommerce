package com.exemple.ecommerce.api.core.exception;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path.Node;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

@Provider
public class ValidationExceptionProvider implements ExceptionMapper<ConstraintViolationException> {

    @Autowired
    private MessageSource messageSource;

    @Context
    private ContainerRequestContext requestContext;

    @Override
    public Response toResponse(ConstraintViolationException e) {

        Map<String, String> messages = e.getConstraintViolations().stream().collect(Collectors.toMap((ConstraintViolation<?> violation) -> {

            String field = null;
            Iterator<Node> nodes = violation.getPropertyPath().iterator();
            while (nodes.hasNext()) {

                field = nodes.next().getName();
            }

            return field;

        }, violation -> messageSource.getMessage(violation.getMessage(), new Object[0], violation.getMessage(),
                requestContext.getAcceptableLanguages().stream().findFirst().orElseGet(Locale::getDefault))));

        return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity(messages).build();
    }
}
