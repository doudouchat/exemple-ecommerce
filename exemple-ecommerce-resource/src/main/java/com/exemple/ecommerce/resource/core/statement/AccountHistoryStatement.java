package com.exemple.ecommerce.resource.core.statement;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.exemple.ecommerce.resource.account.model.AccountHistory;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.exemple.ecommerce.resource.core.dao.AccountHistoryDao;
import com.exemple.ecommerce.resource.core.mapper.AccountHistoryMapper;

@Component
public class AccountHistoryStatement {

    public static final String TABLE = "account_history";

    public static final String ID = "id";

    private static final Logger LOG = LoggerFactory.getLogger(AccountHistoryStatement.class);

    private final CqlSession session;

    public AccountHistoryStatement(CqlSession session) {
        this.session = session;
    }

    public List<AccountHistory> findById(UUID id) {

        AccountHistoryMapper mapper = AccountHistoryMapper.builder(session).withDefaultKeyspace(ResourceExecutionContext.get().keyspace()).build();
        AccountHistoryDao accountHistoryDao = mapper.accountHistoryDao();

        return accountHistoryDao.findById(id).all();
    }

    public Collection<BoundStatement> insert(Collection<AccountHistory> accountHistories) {

        PreparedStatement prepared = session
                .prepare("INSERT INTO " + ResourceExecutionContext.get().keyspace() + ".account_history (id,date,field,value) VALUES (?,?,?,?)");

        return accountHistories.stream()

                .map((AccountHistory history) -> {
                    LOG.debug("save history account {} {} {}", history.getId(), history.getField(), history.getValue());
                    return prepared.bind(history.getId(), history.getDate(), history.getField(), history.getValue());
                }).collect(Collectors.toList());

    }

}
