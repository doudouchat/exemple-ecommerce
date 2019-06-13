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

    @Around("execution(public void com.exemple.ecommerce.resource.login.LoginResource.save(*)) && args (source)")
    void saveLogin(ProceedingJoinPoint joinPoint, JsonNode source) {

        if (source != null) {

            def jsonSlurper = new JsonSlurper()
            def sourceJson = jsonSlurper.parseText(source.toString())

            source =  JsonNodeUtils.create(loginServiceResource.saveLogin(new JsonBuilder(sourceJson).content))
        } else {
            source = JsonNodeUtils.init()
        }

        joinPoint.proceed(source)
    }

    @Around("execution(public void com.exemple.ecommerce.resource.login.LoginResource.save(*, *)) && args (login,source)")
    void updateLogin(ProceedingJoinPoint joinPoint, String login, JsonNode source) {

        if (source != null) {

            def jsonSlurper = new JsonSlurper()
            def sourceJson = jsonSlurper.parseText(source.toString())

            source =  JsonNodeUtils.create(loginServiceResource.updateLogin(new JsonBuilder(sourceJson).content))
        }

        joinPoint.proceed(login, source)
    }
}
