package com.exemple.ecommerce.resource.account.impl;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.exemple.ecommerce.resource.account.AccountResource;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.common.StringHelper;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.exemple.ecommerce.resource.core.helper.OperationHelper;
import com.exemple.ecommerce.resource.core.statement.AccountHistoryStatement;
import com.exemple.ecommerce.resource.core.statement.AccountStatement;
import com.exemple.ecommerce.resource.core.statement.LoginStatement;
import com.exemple.ecommerce.resource.core.statement.ParameterStatement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

@Service
@Validated
public class AccountResourceImpl implements AccountResource {

    private static final Logger LOG = LoggerFactory.getLogger(AccountResourceImpl.class);

    private static final String STATUS = "status";

    private Session session;

    private AccountStatement accountStatement;

    private AccountHistoryStatement accountHistoryStatement;

    private ParameterStatement parameterStatement;

    public AccountResourceImpl(Session session, AccountStatement accountStatement, AccountHistoryStatement accountHistoryStatement,
            ParameterStatement parameterStatement) {
        this.session = session;
        this.accountStatement = accountStatement;
        this.accountHistoryStatement = accountHistoryStatement;
        this.parameterStatement = parameterStatement;
    }

    @Override
    public JsonNode save(UUID id, JsonNode source) {

        LOG.debug("save account {} {}", id, source);

        OffsetDateTime now = ResourceExecutionContext.get().getDate();

        JsonNode accountNode = JsonNodeUtils.clone(source);
        JsonNodeUtils.set(accountNode, id, LoginStatement.ID);
        Statement account = accountStatement.insert(accountNode);

        BatchStatement batch = new BatchStatement();
        batch.add(account);
        this.createHistories(id, accountNode, now).stream().forEach(batch::add);
        session.execute(batch);

        return accountNode;

    }

    private List<Statement> createHistories(final UUID id, JsonNode source, OffsetDateTime now) {

        BinaryOperator<String> function = (n1, n2) -> n2;

        Map<String, Boolean> historyFields = parameterStatement.getHistories();

        return JsonNodeUtils.stream(source.fields()).filter(e -> historyFields.containsKey(e.getKey())).flatMap((Map.Entry<String, JsonNode> e) -> {

            if (historyFields.get(e.getKey())) {

                if (JsonNodeType.OBJECT == e.getValue().getNodeType()) {

                    return JsonNodeUtils.stream(e.getValue().fields()).map(node -> Collections
                            .singletonMap(e.getKey().concat("/").concat(node.getKey()), node.getValue()).entrySet().iterator().next());
                }

                if (JsonNodeType.ARRAY == e.getValue().getNodeType()) {

                    return JsonNodeUtils.stream(e.getValue().elements()).map((JsonNode node) -> {

                        String key = JsonNodeType.OBJECT == node.getNodeType()
                                ? JsonNodeUtils.stream(node.elements()).reduce("", (root, n) -> StringHelper.join(root, n.asText(), '.'), function)
                                : node.asText();

                        return Collections.singletonMap(e.getKey().concat("/").concat(key), node).entrySet().iterator().next();
                    });
                }

            }

            return Collections.singleton(e).stream();

        }).map((Map.Entry<String, JsonNode> e) -> {

            LOG.debug("save history account {} {}", id, e.getKey());

            JsonNode history = JsonNodeUtils.init();
            JsonNodeUtils.set(history, id, AccountHistoryStatement.ID);
            JsonNodeUtils.set(history, e.getKey(), AccountHistoryStatement.FIELD);
            JsonNodeUtils.set(history, now.toString(), AccountHistoryStatement.DATE);

            return accountHistoryStatement.insert(history);
        }).collect(Collectors.toList());
    }

    @Override
    public JsonNode update(UUID id, JsonNode source) {

        LOG.debug("update account {} {}", id, source);

        OffsetDateTime now = ResourceExecutionContext.get().getDate();

        JsonNode origin = this.getById(id).orElseThrow(IllegalArgumentException::new);

        Statement update = accountStatement.update(id, source);

        BatchStatement batch = new BatchStatement();
        batch.setConsistencyLevel(ConsistencyLevel.QUORUM);
        batch.add(update);
        JsonNode account = OperationHelper.diff(source, origin, accountStatement.getTableMetadata());
        if (account.elements().hasNext()) {
            this.createHistories(id, account, now).stream().forEach(batch::add);
        }
        session.execute(batch);

        return account;
    }

    @Override
    public Optional<JsonNode> get(UUID id) {

        Optional<JsonNode> source = getById(id);

        source.ifPresent(node -> LOG.debug("get account {} {}", id, node));

        return source;
    }

    private Optional<JsonNode> getById(UUID id) {

        JsonNode source = accountStatement.get(id, ConsistencyLevel.QUORUM);

        return Optional.ofNullable(source);
    }

    @Override
    public JsonNode getByStatus(String status) {

        JsonNode node = accountStatement.getByIndex("accounts", STATUS, status);

        LOG.debug("get account by status {}:{}", status, node);

        return node;
    }
}
