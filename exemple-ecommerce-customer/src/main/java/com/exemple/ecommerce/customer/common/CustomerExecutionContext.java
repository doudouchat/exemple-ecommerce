package com.exemple.ecommerce.customer.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

public final class CustomerExecutionContext {

    private static ThreadLocal<CustomerExecutionContext> executionContext = new ThreadLocal<>();

    private String app;

    private String version;

    private final Map<UUID, JsonNode> accounts = new HashMap<>();

    private CustomerExecutionContext() {

    }

    public static CustomerExecutionContext get() {

        if (executionContext.get() == null) {
            executionContext.set(new CustomerExecutionContext());
        }

        return executionContext.get();
    }

    public static void destroy() {

        executionContext.remove();
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public JsonNode getAccount(UUID id) {
        return accounts.get(id);
    }

    public void setAccount(UUID id, JsonNode account) {
        this.accounts.put(id, account);
    }

}
