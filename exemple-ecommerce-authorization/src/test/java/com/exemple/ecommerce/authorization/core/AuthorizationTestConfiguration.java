package com.exemple.ecommerce.authorization.core;

import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.mockito.Mockito;
import org.osjava.sj.SimpleJndi;
import org.osjava.sj.loader.JndiLoader;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jndi.JndiObjectFactoryBean;

import com.exemple.ecommerce.application.common.model.ApplicationDetail;
import com.exemple.ecommerce.application.detail.ApplicationDetailService;
import com.exemple.ecommerce.authorization.core.authentication.AuthenticationConfiguration;
import com.exemple.ecommerce.authorization.core.client.AuthorizationClientTestConfiguration;
import com.exemple.ecommerce.authorization.core.feature.FeatureTestConfiguration;
import com.exemple.ecommerce.authorization.core.property.AuthorizationPropertyConfiguration;
import com.exemple.ecommerce.authorization.core.resource.keyspace.AuthorizationResourceKeyspace;
import com.exemple.ecommerce.authorization.core.session.HazelcastHttpSessionConfiguration;
import com.exemple.ecommerce.authorization.core.swagger.SwaggerConfiguration;
import com.exemple.ecommerce.authorization.core.token.AuthorizationTokenConfiguration;
import com.exemple.ecommerce.resource.account.AccountResource;
import com.exemple.ecommerce.resource.login.LoginResource;
import com.google.common.collect.Sets;

@Configuration
@Import({ AuthorizationConfiguration.class, AuthenticationConfiguration.class, AuthorizationTokenConfiguration.class,
        HazelcastHttpSessionConfiguration.class, SwaggerConfiguration.class, AuthorizationClientTestConfiguration.class,
        FeatureTestConfiguration.class })
@ComponentScan(basePackageClasses = AuthorizationResourceKeyspace.class)
@EnableAutoConfiguration
public class AuthorizationTestConfiguration extends AuthorizationPropertyConfiguration {

    @Bean
    public LoginResource loginResource() {
        return Mockito.mock(LoginResource.class);
    }

    @Bean
    public AccountResource accountResource() {
        return Mockito.mock(AccountResource.class);
    }

    @Bean
    public ApplicationDetailService ApplicationDetailService() {

        ApplicationDetailService service = Mockito.mock(ApplicationDetailService.class);

        ApplicationDetail detail = new ApplicationDetail();
        detail.setKeyspace("test");
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
