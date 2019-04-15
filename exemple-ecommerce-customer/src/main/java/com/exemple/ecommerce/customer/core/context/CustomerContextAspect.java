package com.exemple.ecommerce.customer.core.context;

import java.util.Optional;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.exemple.ecommerce.customer.common.CustomerExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class CustomerContextAspect {

    private static final String APP_VERSION_METHOD = "@target(org.springframework.stereotype.Service) "
            + "&& execution(public * com.exemple.ecommerce.customer..*(.., *, *)) && args(.., app, version)";

    @Before(APP_VERSION_METHOD)
    public void beforeValidate(String app, String version) {

        CustomerExecutionContext context = CustomerExecutionContext.get();

        context.setApp(app);
        context.setVersion(version);
    }

    @After(APP_VERSION_METHOD)
    public void afterValidate(String app, String version) {

        CustomerExecutionContext.destroy();
    }

    @AfterThrowing(APP_VERSION_METHOD)
    public void afterValidateException(String app, String version) {

        CustomerExecutionContext.destroy();
    }

    @SuppressWarnings("unchecked")
    @Around("execution(public java.util.Optional<com.fasterxml.jackson.databind.JsonNode> "
            + "com.exemple.ecommerce.resource.account.AccountResource.get(*)) && args(id)")
    public Optional<JsonNode> get(ProceedingJoinPoint joinPoint, UUID id) throws Throwable {

        CustomerExecutionContext context = CustomerExecutionContext.get();

        if (context.getAccount(id) == null) {

            context.setAccount(id, ((Optional<JsonNode>) joinPoint.proceed()).orElse(null));
        }

        return Optional.ofNullable(context.getAccount(id));
    }

    @After("execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.ecommerce.resource.account.AccountResource.update(*, *))")
    public void afterUpdate(JoinPoint joinPoint) {

        CustomerExecutionContext.get().setAccount((UUID) joinPoint.getArgs()[0], null);
    }

}
