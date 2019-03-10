package com.exemple.ecommerce.resource.core.statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class LoginStatement extends StatementResource {

    public static final String ID = "id";

    public static final String LOGIN = "login";

    @Autowired
    private Session session;

    @Autowired
    public LoginStatement(Cluster cluster) {
        super(cluster, LOGIN);
    }

    public JsonNode get(String email) {

        Select select = QueryBuilder.select().json().from(ResourceExecutionContext.get().keyspace(), LOGIN);
        select.where().and(QueryBuilder.eq(LOGIN, email));

        Row row = session.execute(select).one();

        return row != null ? row.get(0, JsonNode.class) : null;
    }

    public Delete delete(String login) {

        Delete delete = QueryBuilder.delete().from(ResourceExecutionContext.get().keyspace(), LOGIN);
        delete.where().and(QueryBuilder.eq(LOGIN, login));

        return delete;
    }

    public Update update(String login, JsonNode source) {

        Update update = update(source);
        update.where().and(QueryBuilder.eq(LOGIN, login));

        return update;
    }

}
