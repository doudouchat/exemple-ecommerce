package com.exemple.ecommerce.customer.account.impl;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.exemple.ecommerce.customer.account.AccountService;
import com.exemple.ecommerce.customer.account.exception.AccountServiceException;
import com.exemple.ecommerce.customer.account.exception.AccountServiceNotFoundException;
import com.exemple.ecommerce.customer.account.validation.AccountValidation;
import com.exemple.ecommerce.customer.core.CustomerExecutionContext;
import com.exemple.ecommerce.event.model.EventData;
import com.exemple.ecommerce.event.model.EventType;
import com.exemple.ecommerce.resource.account.AccountResource;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.exemple.ecommerce.resource.login.LoginResource;
import com.exemple.ecommerce.resource.login.exception.LoginResourceException;
import com.exemple.ecommerce.schema.filter.SchemaFilter;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class AccountServiceImpl implements AccountService {

    private static final String ACCOUNT = "account";

    private AccountResource accountResource;

    private LoginResource loginResource;

    private AccountValidation accountValidation;

    private SchemaFilter schemaFilter;

    private ApplicationEventPublisher applicationEventPublisher;

    public AccountServiceImpl(AccountResource accountResource, LoginResource loginResource, AccountValidation accountValidation,
            SchemaFilter schemaFilter, ApplicationEventPublisher applicationEventPublisher) {
        this.accountResource = accountResource;
        this.loginResource = loginResource;
        this.accountValidation = accountValidation;
        this.schemaFilter = schemaFilter;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public JsonNode save(JsonNode source) throws AccountServiceException {

        accountValidation.validate(source, null);

        UUID id = UUID.randomUUID();

        try {
            loginResource.save(id, source);
        } catch (LoginResourceException e) {
            throw new AccountServiceException(e);
        }

        JsonNode account = accountResource.save(id, source);

        CustomerExecutionContext context = CustomerExecutionContext.get();
        String app = context.getApp();
        String version = context.getVersion();

        EventData eventData = new EventData(account, ACCOUNT, EventType.CREATE, app, version, ResourceExecutionContext.get().getDate().toString());
        applicationEventPublisher.publishEvent(eventData);

        return schemaFilter.filter(context.getApp(), context.getVersion(), ACCOUNT, account);
    }

    @Override
    public JsonNode save(UUID id, JsonNode source) throws AccountServiceException {

        JsonNode old = accountResource.get(id).orElseThrow(AccountServiceNotFoundException::new);

        accountValidation.validate(source, old);

        try {
            loginResource.update(id, source);
        } catch (LoginResourceException e) {
            throw new AccountServiceException(e);
        }

        JsonNode account = accountResource.update(id, source);

        CustomerExecutionContext context = CustomerExecutionContext.get();
        String app = context.getApp();
        String version = context.getVersion();

        EventData eventData = new EventData(account, ACCOUNT, EventType.UPDATE, app, version, ResourceExecutionContext.get().getDate().toString());
        applicationEventPublisher.publishEvent(eventData);

        return schemaFilter.filter(context.getApp(), context.getVersion(), ACCOUNT, account);
    }

    @Override
    public JsonNode get(UUID id) throws AccountServiceNotFoundException {

        JsonNode account = accountResource.get(id).orElseThrow(AccountServiceNotFoundException::new);
        CustomerExecutionContext context = CustomerExecutionContext.get();

        return schemaFilter.filter(context.getApp(), context.getVersion(), ACCOUNT, account);
    }
}
