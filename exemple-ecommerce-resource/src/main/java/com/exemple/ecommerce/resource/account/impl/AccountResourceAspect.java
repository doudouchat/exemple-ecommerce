package com.exemple.ecommerce.resource.account.impl;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Aspect
@Component
public class AccountResourceAspect {

    @Before("execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.ecommerce.resource.account.AccountResource.save(*, *)) "
            + "&& args(id, source)")
    public void beforeSave(UUID id, JsonNode source) {

        JsonNodeUtils.filter(source);
    }

    @Before("execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.ecommerce.resource.account.AccountResource.update(*, *)) "
            + "&& args(id, source)")
    public void beforeUpdate(UUID id, JsonNode source) {

        filter(source);
    }

    private static void filter(JsonNode source) {

        JsonNodeUtils.filter(source, (Map.Entry<String, JsonNode> e) -> {

            if (JsonNodeType.ARRAY == e.getValue().getNodeType()) {

                ((ObjectNode) source).replace(e.getKey(), JsonNodeUtils.create(JsonNodeUtils.stream(e.getValue().elements())
                        .filter(node -> JsonNodeType.NULL != node.getNodeType()).collect(Collectors.toList())));

                filter(source.get(e.getKey()));
            }
        });
    }

    @After("execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.ecommerce.resource.account.AccountResource.update(*, *))")
    public void afterUpdate(JoinPoint joinPoint) {

        ResourceExecutionContext.get().setAccount((UUID) joinPoint.getArgs()[0], null);
    }

    @AfterReturning(pointcut = "execution(public com.fasterxml.jackson.databind.JsonNode "
            + "com.exemple.ecommerce.resource.account.AccountResource.*(..))", returning = "source")
    public void afterReturning(JsonNode source) {

        JsonNodeUtils.filter(source);

    }

    @AfterReturning(pointcut = "execution(public java.util.Optional<com.fasterxml.jackson.databind.JsonNode> "
            + "com.exemple.ecommerce.resource.account.AccountResource.*(..))", returning = "source")
    public void afterReturning(Optional<JsonNode> source) {

        source.ifPresent(JsonNodeUtils::filter);

    }

    @SuppressWarnings("unchecked")
    @Around("execution(public java.util.Optional<com.fasterxml.jackson.databind.JsonNode> "
            + "com.exemple.ecommerce.resource.account.AccountResource.get(*)) && args(id)")
    public Optional<JsonNode> get(ProceedingJoinPoint joinPoint, UUID id) throws Throwable {

        ResourceExecutionContext context = ResourceExecutionContext.get();

        if (context.getAccount(id) == null) {

            context.setAccount(id, ((Optional<JsonNode>) joinPoint.proceed()).orElse(null));
        }

        return Optional.ofNullable(context.getAccount(id));
    }

}
