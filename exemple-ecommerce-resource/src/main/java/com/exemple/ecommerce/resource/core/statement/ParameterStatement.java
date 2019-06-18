package com.exemple.ecommerce.resource.core.statement;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ParameterStatement extends StatementResource {

    public static final String TABLE = "parameter";

    public static final String APP = "app";

    public static final String APP_DEFAULT = "default";

    private final Session session;

    public ParameterStatement(Cluster cluster, Session session) {
        super(cluster, TABLE);
        this.session = session;
    }

    public JsonNode get(String parameter) {

        Select select = QueryBuilder.select().toJson(parameter).from(ResourceExecutionContext.get().keyspace(), TABLE);
        select.where().and(QueryBuilder.eq(APP, APP_DEFAULT));

        return Optional.of(session.execute(select).one()).orElseThrow(IllegalArgumentException::new).get(0, JsonNode.class);
    }

    @Cacheable("parameter_histories")
    public Map<String, Boolean> getHistories() {
        return JsonNodeUtils.stream(this.get("histories").fields()).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().booleanValue()));
    }

}
