package com.exemple.ecommerce.customer.connexion.resource

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Autowired

import com.exemple.ecommerce.resource.common.JsonNodeUtils
import com.fasterxml.jackson.databind.JsonNode

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

@Aspect
class LoginServiceResourceAspect {

    @Autowired
    private LoginServiceResource loginServiceResource

    @Around("execution(public void com.exemple.ecommerce.resource.login.LoginResource.save(*, *)) && args (id,source)")
    void saveLogin(ProceedingJoinPoint joinPoint, UUID id, JsonNode source) {

        if (source != null) {

            def jsonSlurper = new JsonSlurper()
            def accountJson = jsonSlurper.parseText(source.toString())

            source =  JsonNodeUtils.create(loginServiceResource.saveLogin(id, new JsonBuilder(accountJson).content))
        }

        joinPoint.proceed(id, source)
    }
    
    @Around("execution(public void com.exemple.ecommerce.resource.login.LoginResource.update(*, *)) && args (id,source)")
    void updateLogin(ProceedingJoinPoint joinPoint, UUID id, JsonNode source) {

        if (source != null) {

            def jsonSlurper = new JsonSlurper()
            def accountJson = jsonSlurper.parseText(source.toString())

            source =  JsonNodeUtils.create(loginServiceResource.updateLogin(id, new JsonBuilder(accountJson).content))
        }

        joinPoint.proceed(id, source)
    }
}
