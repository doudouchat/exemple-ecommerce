package com.exemple.ecommerce.api.core;

import java.io.File;
import java.io.IOException;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.mock.jndi.ExpectedLookupTemplate;

import com.exemple.ecommerce.api.core.authorization.AuthorizationService;
import com.exemple.ecommerce.application.common.model.ApplicationDetail;
import com.exemple.ecommerce.application.detail.ApplicationDetailService;
import com.exemple.ecommerce.customer.account.AccountService;
import com.exemple.ecommerce.customer.login.LoginService;
import com.exemple.ecommerce.customer.subcription.SubscriptionService;
import com.exemple.ecommerce.resource.schema.SchemaResource;
import com.exemple.ecommerce.schema.description.SchemaDescription;
import com.exemple.ecommerce.schema.validation.SchemaValidation;
import com.exemple.ecommerce.store.stock.StockService;

@Configuration
public class ApiTestConfiguration extends ApiConfiguration {

    @Bean
    public AccountService accountService() {
        return Mockito.mock(AccountService.class);
    }

    @Bean
    public SchemaDescription schemaService() {
        return Mockito.mock(SchemaDescription.class);
    }

    @Bean
    public SchemaValidation schemaValidation() {
        return Mockito.mock(SchemaValidation.class);
    }

    @Bean
    public LoginService loginService() {
        return Mockito.mock(LoginService.class);
    }

    @Bean
    public StockService stockService() {
        return Mockito.mock(StockService.class);
    }

    @Bean
    public SchemaResource schemaResource() {
        return Mockito.mock(SchemaResource.class);
    }

    @Bean
    public SubscriptionService subscriptionService() {
        return Mockito.mock(SubscriptionService.class);
    }

    @Bean(name = "authorizationServiceImpl")
    public AuthorizationService AuthorizationService() {
        return Mockito.mock(AuthorizationService.class);
    }

    @Bean
    public ApplicationDetailService ApplicationDetailService() {

        ApplicationDetailService service = Mockito.mock(ApplicationDetailService.class);

        ApplicationDetail detail = new ApplicationDetail();
        detail.setKeyspace("test");
        detail.setCompany("company1");

        Mockito.when(service.get(Mockito.anyString())).thenReturn(detail);

        return service;
    }

    @Bean
    @Override
    public JndiObjectFactoryBean jndiObjectFactoryBean() {

        JndiObjectFactoryBean jndiObjectFactoryBean = super.jndiObjectFactoryBean();

        try {
            File tempFile = File.createTempFile("test-", ".tmp");
            tempFile.deleteOnExit();
            jndiObjectFactoryBean.setJndiTemplate(new ExpectedLookupTemplate(JNDI_NAME, new FileSystemResource(tempFile).getURL().toString()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return jndiObjectFactoryBean;
    }

}
