package com.exemple.ecommerce.customer.common;

public final class CustomerExecutionContext {

    private static ThreadLocal<CustomerExecutionContext> executionContext = new ThreadLocal<>();

    private String app;

    private String version;

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

}
