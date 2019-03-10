package com.exemple.ecommerce.customer.account.validation.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.exemple.ecommerce.customer.account.validation.AccountValidation;
import com.exemple.ecommerce.customer.core.CustomerExecutionContext;
import com.exemple.ecommerce.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;

@Component
@Validated
class AccountValidationImpl implements AccountValidation {

    @Autowired
    private SchemaValidation schemaValidation;

    @Override
    public void validate(JsonNode form, JsonNode old) {

        CustomerExecutionContext context = CustomerExecutionContext.get();
        schemaValidation.validate(context.getApp(), context.getVersion(), "account", form, old);
    }
}
