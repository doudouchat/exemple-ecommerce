package com.exemple.ecommerce.schema.validation.impl;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.exemple.ecommerce.schema.validation.SchemaValidationContext;
import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class SchemaValidationAspect {

    @Before("execution(public void com.exemple.ecommerce.schema.validation.SchemaValidation.validate(*, *, *, *, *)) "
            + "&& args(app, version, resource, form, old)")
    public void beforeValidate(String app, String version, String resource, JsonNode form, JsonNode old) {

        SchemaValidationContext context = SchemaValidationContext.get();

        context.setApp(app);
        context.setVersion(version);
        context.setResource(resource);
    }

    @After("execution(public void com.exemple.ecommerce.schema.validation.SchemaValidation.validate(..))")
    public void afterValidate() {

        SchemaValidationContext.destroy();
    }

    @AfterThrowing("execution(public void com.exemple.ecommerce.schema.validation.SchemaValidation.validate(..))")
    public void afterValidateException() {

        SchemaValidationContext.destroy();
    }

}
