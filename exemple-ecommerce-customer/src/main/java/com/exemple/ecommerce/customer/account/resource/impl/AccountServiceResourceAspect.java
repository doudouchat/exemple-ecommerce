package com.exemple.ecommerce.customer.account.resource.impl;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.exemple.ecommerce.customer.account.resource.AccountServiceResource;
import com.exemple.ecommerce.customer.common.CustomerExecutionContext;
import com.exemple.ecommerce.resource.common.JsonNodeTransformUtils;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.schema.SchemaResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Aspect
@Component
class AccountServiceResourceAspect {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AccountServiceResource accountServiceResource;

    private final SchemaResource schemaResource;

    public AccountServiceResourceAspect(AccountServiceResource accountServiceResource, SchemaResource schemaResource) {
        this.accountServiceResource = accountServiceResource;
        this.schemaResource = schemaResource;
    }

    @Around("execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.ecommerce.resource.account.AccountResource.save(*, *)) "
            + "&& args (id,account)")
    public JsonNode save(ProceedingJoinPoint joinPoint, UUID id, JsonNode account) throws Throwable {

        if (account != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> accountMap = MAPPER.convertValue(account, Map.class);
            return transform((JsonNode) joinPoint.proceed(new Object[] { id, JsonNodeUtils.create(accountServiceResource.save(id, accountMap)) }));
        }

        return (JsonNode) joinPoint.proceed(new Object[] { id, null });

    }

    @Around("execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.ecommerce.resource.account.AccountResource.*(*, *)) "
            + "&& args (id,account)")
    public JsonNode saveOrUpdateAccount(ProceedingJoinPoint joinPoint, UUID id, JsonNode account) throws Throwable {

        if (account != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> accountMap = MAPPER.convertValue(account, Map.class);
            return transform((JsonNode) joinPoint
                    .proceed(new Object[] { id, JsonNodeUtils.create(accountServiceResource.saveOrUpdateAccount(id, accountMap)) }));
        }

        return (JsonNode) joinPoint.proceed(new Object[] { id, null });

    }

    @Around("execution(public java.util.Optional<com.fasterxml.jackson.databind.JsonNode> "
            + "com.exemple.ecommerce.resource.account.AccountResource.get(*))")
    public Optional<JsonNode> get(ProceedingJoinPoint joinPoint) throws Throwable {

        @SuppressWarnings("unchecked")
        Optional<JsonNode> account = (Optional<JsonNode>) joinPoint.proceed();
        return account.map(this::transform);
    }

    private JsonNode transform(JsonNode node) {

        CustomerExecutionContext context = CustomerExecutionContext.get();
        Set<String> dateTime = schemaResource.getTransform(context.getApp(), context.getVersion(), "account").get("date_time");
        return JsonNodeTransformUtils.transformDate(node, dateTime.toArray(new String[0]));
    }

}
