package com.exemple.ecommerce.customer.account.resource

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Autowired

import com.exemple.ecommerce.customer.core.CustomerExecutionContext
import com.exemple.ecommerce.resource.common.JsonNodeTransformUtils
import com.exemple.ecommerce.resource.common.JsonNodeUtils
import com.exemple.ecommerce.resource.schema.SchemaResource
import com.fasterxml.jackson.databind.JsonNode

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

@Aspect
class AccountServiceResourceAspect {

    @Autowired
    private AccountServiceResource accountServiceResource

    @Autowired
    private SchemaResource schemaResource

    @Around("execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.ecommerce.resource.account.AccountResource.save(*, *)) && args (id,account)")
    JsonNode save(ProceedingJoinPoint joinPoint, UUID id, JsonNode account) {

        if (account != null) {

            def jsonSlurper = new JsonSlurper()
            def accountJson = jsonSlurper.parseText(account.toString())

            return transform(((JsonNode) joinPoint.proceed(id, JsonNodeUtils.create(accountServiceResource.save(id, new JsonBuilder(accountJson).content)))))
        }

        return (JsonNode) joinPoint.proceed(id, account)
    }

    @Around("execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.ecommerce.resource.account.AccountResource.*(*, *)) && args (id,account)")
    JsonNode saveOrUpdateAccount(ProceedingJoinPoint joinPoint, UUID id, JsonNode account) {

        if (account != null) {

            def jsonSlurper = new JsonSlurper()
            def accountJson = jsonSlurper.parseText(account.toString())

            return transform((JsonNode) joinPoint.proceed(id, JsonNodeUtils.create(accountServiceResource.saveOrUpdateAccount(id, new JsonBuilder(accountJson).content))))
        }

        return (JsonNode) joinPoint.proceed(id, account)
    }

    @Around("execution(public java.util.Optional<com.fasterxml.jackson.databind.JsonNode> com.exemple.ecommerce.resource.account.AccountResource.get(*))")
    Optional<JsonNode> get(ProceedingJoinPoint joinPoint) {

        Optional<JsonNode> account = joinPoint.proceed()

        if (account != null && account.isPresent()) {

            return Optional.of(transform(account.get()))
        }

        return account
    }

    private JsonNode transform(JsonNode node) {

        CustomerExecutionContext context = CustomerExecutionContext.get()
        Set dateTime = schemaResource.getTransform(context.getApp(), context.getVersion(), "account").get("date_time")
        if(dateTime == null) {
            dateTime = Collections.emptySet()
        }
        return JsonNodeTransformUtils.transformDate(node, dateTime.toArray(new String[0]))
    }
}
