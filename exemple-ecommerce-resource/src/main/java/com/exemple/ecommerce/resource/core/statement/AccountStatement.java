package com.exemple.ecommerce.resource.core.statement;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class AccountStatement extends StatementResource {

    public static final String TABLE = "account";

    public static final String ID = "id";

    private final Session session;

    public AccountStatement(Cluster cluster, Session session) {
        super(cluster, TABLE);
        this.session = session;
    }

    public Update update(UUID id, JsonNode source) {

        Update update = update(source);
        update.where(QueryBuilder.eq(ID, id));

        return update;
    }

    public JsonNode get(UUID id) {

        return get(id, null);
    }

    public JsonNode get(UUID id, ConsistencyLevel consistency) {

        Select select = QueryBuilder.select().json().all().from(ResourceExecutionContext.get().keyspace(), TABLE);
        select.where().and(QueryBuilder.eq(ID, id));
        select.setConsistencyLevel(consistency);

        Row row = session.execute(select).one();

        return row != null ? row.get(0, JsonNode.class) : null;
    }

    public JsonNode getByIndex(String root, String field, Object value) {

        JsonNode node = JsonNodeUtils.init(root);

        Select select = QueryBuilder.select().json().all().from(ResourceExecutionContext.get().keyspace(), TABLE);
        select.where(QueryBuilder.eq(field, value));

        List<Row> rows = session.execute(select).all();

        JsonNodeUtils.set(node, rows.stream().map(row -> row.get(0, JsonNode.class)).collect(Collectors.toSet()), root);

        return node;
    }

}
