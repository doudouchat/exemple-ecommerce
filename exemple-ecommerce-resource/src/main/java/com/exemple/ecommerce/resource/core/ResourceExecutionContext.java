package com.exemple.ecommerce.resource.core;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.JsonNode;

public final class ResourceExecutionContext {

    private static ThreadLocal<ResourceExecutionContext> executionContext = new ThreadLocal<>();

    private OffsetDateTime date = OffsetDateTime.now();

    private final Map<UUID, JsonNode> accounts = new HashMap<>();

    private String keyspace;

    private ResourceExecutionContext() {

    }

    public static ResourceExecutionContext get() {

        if (executionContext.get() == null) {
            executionContext.set(new ResourceExecutionContext());
        }

        return executionContext.get();
    }

    public static void destroy() {

        executionContext.remove();
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public JsonNode getAccount(UUID id) {
        return accounts.get(id);
    }

    public void setAccount(UUID id, JsonNode account) {
        this.accounts.put(id, account);
    }

    public String keyspace() {

        Assert.notNull(keyspace, "keyspace in ResourceExecutionContext must be required");

        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }
}
