package com.exemple.ecommerce.resource.core.statement;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class AccountLastHistoryStatement extends StatementResource {

    public static final String TABLE = "account_last_history";

    public static final String ID = "id";

    public static final String FIELD = "field";

    public static final String DATE = "date";

    @Autowired
    private Session session;

    @Autowired
    public AccountLastHistoryStatement(Cluster cluster) {
        super(cluster, TABLE);
    }

    public List<JsonNode> findById(UUID id) {

        Select select = QueryBuilder.select().json().all().from(ResourceExecutionContext.get().keyspace(), TABLE);
        select.where(QueryBuilder.eq(ID, id));

        return session.execute(select).all().stream().map(row -> row.get(0, JsonNode.class)).collect(Collectors.toList());
    }

}
