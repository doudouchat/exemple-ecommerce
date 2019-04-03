package com.exemple.ecommerce.resource.core.statement;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.exemple.ecommerce.resource.account.model.AccountHistory;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;

@Component
public class AccountHistoryStatement extends StatementResource {

    public static final String TABLE = "account_history";

    public static final String ID = "id";

    private static final Logger LOG = LoggerFactory.getLogger(AccountHistoryStatement.class);

    private final Session session;

    public AccountHistoryStatement(Cluster cluster, Session session) {
        super(cluster, TABLE);
        this.session = session;
    }

    public List<AccountHistory> findById(UUID id) {

        String keyspace = ResourceExecutionContext.get().keyspace();
        MappingManager manager = new MappingManager(session);
        Mapper<AccountHistory> mapper = manager.mapper(AccountHistory.class, keyspace);

        Select select = QueryBuilder.select().from(keyspace, TABLE);
        select.where().and(QueryBuilder.eq(ID, id));

        return mapper.map(session.execute(select)).all();
    }

    public Collection<Statement> insert(Collection<AccountHistory> accountHistories) {

        PreparedStatement prepared = session
                .prepare("INSERT INTO " + ResourceExecutionContext.get().keyspace() + ".account_history (id,date,field,value) VALUES (?,?,?,?)");

        return accountHistories.stream()

                .map((AccountHistory history) -> {
                    LOG.debug("save history account {} {} {}", history.getId(), history.getField(), history.getValue());
                    return prepared.bind(history.getId(), history.getDate(), history.getField(), history.getValue());
                }).collect(Collectors.toList());

    }

}
