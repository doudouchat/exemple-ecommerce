package com.exemple.ecommerce.resource.core.statement;

import org.springframework.stereotype.Component;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class SubscriptionStatement extends StatementResource {

    public static final String EMAIL = "email";

    public static final String SUBSCRIPTION = "subscription";

    private final Session session;

    public SubscriptionStatement(Cluster cluster, Session session) {
        super(cluster, SUBSCRIPTION);
        this.session = session;
    }

    public JsonNode get(String email) {

        Select select = QueryBuilder.select().json().from(ResourceExecutionContext.get().keyspace(), SUBSCRIPTION);
        select.where().and(QueryBuilder.eq(EMAIL, email));

        Row row = session.execute(select).one();

        return row != null ? row.get(0, JsonNode.class) : null;
    }

}
