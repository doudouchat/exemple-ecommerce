package com.exemple.ecommerce.customer.core;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.exemple.ecommerce.resource.account.AccountResource;
import com.exemple.ecommerce.resource.login.LoginResource;
import com.exemple.ecommerce.resource.schema.SchemaResource;
import com.exemple.ecommerce.schema.filter.SchemaFilter;
import com.exemple.ecommerce.schema.validation.SchemaValidation;

@Configuration
public class CustomerTestConfiguration extends CustomerConfiguration {

    @Bean
    public AccountResource accountResource() {
        return Mockito.mock(AccountResource.class);
    }

    @Bean
    public LoginResource loginResource() {
        return Mockito.mock(LoginResource.class);
    }

    @Bean
    public SchemaValidation schemaValidation() {
        return Mockito.mock(SchemaValidation.class);
    }

    @Bean
    public SchemaFilter schemaFilter() {
        return Mockito.mock(SchemaFilter.class);
    }

    @Bean
    public SchemaResource schemaResource() {
        return Mockito.mock(SchemaResource.class);
    }

}
