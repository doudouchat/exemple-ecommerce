package com.exemple.ecommerce.api.core;

import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.mockito.Mockito;
import org.osjava.sj.SimpleJndi;
import org.osjava.sj.loader.JndiLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jndi.JndiObjectFactoryBean;

import com.exemple.ecommerce.api.core.authorization.AuthorizationService;
import com.exemple.ecommerce.application.common.model.ApplicationDetail;
import com.exemple.ecommerce.application.detail.ApplicationDetailService;
import com.exemple.ecommerce.customer.account.AccountService;
import com.exemple.ecommerce.customer.login.LoginService;
import com.exemple.ecommerce.customer.subcription.SubscriptionService;
import com.exemple.ecommerce.resource.login.LoginResource;
import com.exemple.ecommerce.resource.schema.SchemaResource;
import com.exemple.ecommerce.schema.description.SchemaDescription;
import com.exemple.ecommerce.schema.validation.SchemaValidation;
import com.exemple.ecommerce.store.stock.StockService;
import com.google.common.collect.Sets;

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
    public LoginResource loginResource() {
        return Mockito.mock(LoginResource.class);
    }

    @Bean
    public ApplicationDetailService ApplicationDetailService() {

        ApplicationDetailService service = Mockito.mock(ApplicationDetailService.class);

        ApplicationDetail detail = new ApplicationDetail();
        detail.setKeyspace("test");
        detail.setCompany("company1");
        detail.setClientIds(Sets.newHashSet("clientId1"));

        Mockito.when(service.get(Mockito.anyString())).thenReturn(detail);

        return service;
    }

    @Bean
    public InitialContext initialContext() throws NamingException, IOException {

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.SimpleContextFactory");
        System.setProperty(SimpleJndi.ENC, "java:comp");
        System.setProperty(JndiLoader.COLON_REPLACE, "--");
        System.setProperty(JndiLoader.DELIMITER, "/");
        System.setProperty(SimpleJndi.SHARED, "true");
        System.setProperty(SimpleJndi.ROOT, new ClassPathResource("java--comp").getURL().getFile());

        return new InitialContext();

    }

    @Bean
    @DependsOn("initialContext")
    @Override
    public JndiObjectFactoryBean jndiObjectFactoryBean() {

        return super.jndiObjectFactoryBean();
    }

}
