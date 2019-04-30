package com.exemple.ecommerce.resource.subscription.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.driver.core.Session;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.core.statement.SubscriptionStatement;
import com.exemple.ecommerce.resource.subscription.SubscriptionResource;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Validated
public class SubscriptionResourceImpl implements SubscriptionResource {

    private Session session;

    private SubscriptionStatement subscriptionStatement;

    public SubscriptionResourceImpl(SubscriptionStatement subscriptionStatement, Session session) {
        this.subscriptionStatement = subscriptionStatement;
        this.session = session;
    }

    @Override
    public Optional<JsonNode> get(String email) {

        JsonNode source = subscriptionStatement.get(email);

        return Optional.ofNullable(source);
    }

    @Override
    public void save(String email, JsonNode source) {

        JsonNode subscription = JsonNodeUtils.clone(source);
        JsonNodeUtils.set(subscription, email, SubscriptionStatement.EMAIL);

        session.execute(subscriptionStatement.insert(subscription));

    }

}
