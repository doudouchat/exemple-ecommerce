package com.exemple.ecommerce.resource.core.statement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class AccountHistoryStatement extends StatementResource {

    public static final String TABLE = "account_history";

    public static final String ID = "id";

    public static final String FIELD = "field";

    public static final String DATE = "date";

    @Autowired
    private Session session;

    @Autowired
    public AccountHistoryStatement(Cluster cluster) {
        super(cluster, TABLE);
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
