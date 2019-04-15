package com.exemple.ecommerce.application.common.model;

import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ApplicationDetail {

    @NotBlank
    private String keyspace;

    @NotBlank
    private String company;

    private Long expiryTimePassword;

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Long getExpiryTimePassword() {
        return expiryTimePassword;
    }

    public void setExpiryTimePassword(Long expiryTimePassword) {
        this.expiryTimePassword = expiryTimePassword;
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("keyspace", keyspace).append("company", company).append("expiryTimePassword", expiryTimePassword)
                .toString();

    }

}
