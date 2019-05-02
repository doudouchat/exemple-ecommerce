package com.exemple.ecommerce.resource.stock.impl;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.exemple.ecommerce.resource.stock.StockResource;

@Service
@Validated
public class StockResourceImpl implements StockResource {

    public static final String TABLE = "stock";

    private Session session;

    public StockResourceImpl(Session session) {
        this.session = session;
    }

    @Override
    public void update(String store, String product, long quantity) {

        Update update = QueryBuilder.update(ResourceExecutionContext.get().keyspace(), TABLE);
        update.with(QueryBuilder.incr("quantity", quantity));
        update.where().and(QueryBuilder.eq("store", store)).and(QueryBuilder.eq("product", product));

        session.execute(update);
    }

    @Override
    public long get(String store, String product) {

        Select select = QueryBuilder.select("quantity").from(ResourceExecutionContext.get().keyspace(), TABLE);
        select.where().and(QueryBuilder.eq("store", store)).and(QueryBuilder.eq("product", product));
        select.setConsistencyLevel(ConsistencyLevel.QUORUM);

        Row row = session.execute(select).one();

        return row != null ? row.getLong(0) : 0L;
    }

}
