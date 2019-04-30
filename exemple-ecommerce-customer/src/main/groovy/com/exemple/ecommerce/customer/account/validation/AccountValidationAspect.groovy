package com.exemple.ecommerce.customer.account.validation

import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Autowired

import com.fasterxml.jackson.databind.JsonNode

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

@Aspect
class AccountValidationAspect {

    @Autowired
    private AccountValidationCustom accountValidationCustom

    @After("execution(public void com.exemple.ecommerce.customer.account.validation.AccountValidation.validate(*, *, ..)) && args(form, old, ..)")
    void validate(JsonNode form, JsonNode old) {

        def jsonSlurper = new JsonSlurper()
        def formJson = jsonSlurper.parseText(form.toString())

        Map<String, ?> formMap = new JsonBuilder(formJson).content

        Map<String, ?> oldMap = null
        if (old != null) {

            def oldFormJson = jsonSlurper.parseText(old.toString())
            oldMap = new JsonBuilder(oldFormJson).content
        }

        accountValidationCustom.validate(formMap, oldMap)
    }
}