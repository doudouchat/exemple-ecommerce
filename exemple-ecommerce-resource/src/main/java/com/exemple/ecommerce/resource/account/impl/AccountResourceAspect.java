package com.exemple.ecommerce.resource.account.impl;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.exemple.ecommerce.resource.common.util.JsonNodeFilterUtils;
import com.exemple.ecommerce.resource.common.util.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Aspect
@Component
public class AccountResourceAspect {

    @Before("execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.ecommerce.resource.account.AccountResource.save(*, *)) "
            + "&& args(id, source)")
    public void beforeSave(UUID id, JsonNode source) {

        JsonNodeFilterUtils.clean(source);
    }

    @Before("execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.ecommerce.resource.account.AccountResource.update(*, *)) "
            + "&& args(id, source)")
    public void beforeUpdate(UUID id, JsonNode source) {

        filter(source);
    }

    private static void filter(JsonNode source) {

        JsonNodeFilterUtils.filter(source, (Map.Entry<String, JsonNode> e) -> {

            if (JsonNodeType.ARRAY == e.getValue().getNodeType()) {

                ((ObjectNode) source).replace(e.getKey(), JsonNodeUtils.create(JsonNodeUtils.stream(e.getValue().elements())
                        .filter(node -> JsonNodeType.NULL != node.getNodeType()).collect(Collectors.toList())));

                filter(source.get(e.getKey()));
            }
        });
    }

    @AfterReturning(pointcut = "execution(public com.fasterxml.jackson.databind.JsonNode "
            + "com.exemple.ecommerce.resource.account.AccountResource.*(..))", returning = "source")
    public void afterReturning(JsonNode source) {

        JsonNodeFilterUtils.clean(source);

    }

    @AfterReturning(pointcut = "execution(public java.util.Optional<com.fasterxml.jackson.databind.JsonNode> "
            + "com.exemple.ecommerce.resource.account.AccountResource.*(..))", returning = "source")
    public void afterReturning(Optional<JsonNode> source) {

        source.ifPresent(JsonNodeFilterUtils::clean);

    }

}
