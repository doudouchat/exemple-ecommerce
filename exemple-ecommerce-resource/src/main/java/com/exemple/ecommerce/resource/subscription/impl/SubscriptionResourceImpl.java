package com.exemple.ecommerce.resource.subscription.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.oss.driver.api.core.CqlSession;
import com.exemple.ecommerce.resource.common.JsonQueryBuilder;
import com.exemple.ecommerce.resource.common.util.JsonNodeUtils;
import com.exemple.ecommerce.resource.core.statement.SubscriptionStatement;
import com.exemple.ecommerce.resource.subscription.SubscriptionResource;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Validated
public class SubscriptionResourceImpl implements SubscriptionResource {

    private final CqlSession session;

    private final SubscriptionStatement subscriptionStatement;

    private final JsonQueryBuilder jsonQueryBuilder;

    public SubscriptionResourceImpl(SubscriptionStatement subscriptionStatement, CqlSession session) {
        this.subscriptionStatement = subscriptionStatement;
        this.session = session;
        this.jsonQueryBuilder = new JsonQueryBuilder(session, SubscriptionStatement.SUBSCRIPTION);
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

        session.execute(jsonQueryBuilder.insert(subscription).build());

    }

}
