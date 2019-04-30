package com.exemple.ecommerce.customer.subcription.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.exemple.ecommerce.customer.subcription.SubscriptionService;
import com.exemple.ecommerce.customer.subcription.exception.SubscriptionServiceNotFoundException;
import com.exemple.ecommerce.customer.subcription.validation.SubscriptionValidation;
import com.exemple.ecommerce.event.model.EventData;
import com.exemple.ecommerce.event.model.EventType;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.exemple.ecommerce.resource.core.statement.SubscriptionStatement;
import com.exemple.ecommerce.resource.subscription.SubscriptionResource;
import com.exemple.ecommerce.schema.filter.SchemaFilter;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private SubscriptionResource subscriptionResource;

    private SubscriptionValidation subscriptionValidation;

    private SchemaFilter schemaFilter;

    private ApplicationEventPublisher applicationEventPublisher;

    public SubscriptionServiceImpl(SubscriptionResource subscriptionResource, SubscriptionValidation subscriptionValidation,
            SchemaFilter schemaFilter, ApplicationEventPublisher applicationEventPublisher) {
        this.subscriptionResource = subscriptionResource;
        this.subscriptionValidation = subscriptionValidation;
        this.schemaFilter = schemaFilter;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public boolean save(String email, JsonNode source, String app, String version) {

        JsonNode subscription = JsonNodeUtils.clone(source);
        JsonNodeUtils.set(subscription, email, SubscriptionStatement.EMAIL);

        subscriptionValidation.validate(subscription, null, app, version);

        boolean created = !subscriptionResource.get(email).isPresent();

        subscriptionResource.save(email, source);

        EventData eventData = new EventData(subscription, "subscription", EventType.CREATE, app, version,
                ResourceExecutionContext.get().getDate().toString());
        applicationEventPublisher.publishEvent(eventData);

        return created;

    }

    @Override
    public JsonNode get(String email, String app, String version) throws SubscriptionServiceNotFoundException {

        JsonNode source = subscriptionResource.get(email).orElseThrow(SubscriptionServiceNotFoundException::new);

        return schemaFilter.filter(app, version, "subscription", source);
    }
}
