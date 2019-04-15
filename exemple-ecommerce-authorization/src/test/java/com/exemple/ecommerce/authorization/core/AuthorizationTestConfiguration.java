package com.exemple.ecommerce.authorization.core;

import java.io.IOException;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.mock.jndi.ExpectedLookupTemplate;

import com.exemple.ecommerce.application.common.model.ApplicationDetail;
import com.exemple.ecommerce.application.detail.ApplicationDetailService;
import com.exemple.ecommerce.authorization.core.authentication.AuthenticationConfiguration;
import com.exemple.ecommerce.authorization.core.client.AuthorizationClientTestConfiguration;
import com.exemple.ecommerce.authorization.core.cors.AuthorizationCorsConfiguration;
import com.exemple.ecommerce.authorization.core.feature.FeatureTestConfiguration;
import com.exemple.ecommerce.authorization.core.property.AuthorizationPropertyConfiguration;
import com.exemple.ecommerce.authorization.core.resource.keyspace.AuthorizationResourceKeyspace;
import com.exemple.ecommerce.authorization.core.session.HazelcastHttpSessionConfiguration;
import com.exemple.ecommerce.authorization.core.swagger.SwaggerConfiguration;
import com.exemple.ecommerce.authorization.core.token.AuthorizationTokenConfiguration;
import com.exemple.ecommerce.resource.account.AccountResource;
import com.exemple.ecommerce.resource.login.LoginResource;

@Configuration
@Import({ AuthorizationConfiguration.class, AuthenticationConfiguration.class, AuthorizationTokenConfiguration.class,
        HazelcastHttpSessionConfiguration.class, SwaggerConfiguration.class, AuthorizationCorsConfiguration.class,
        AuthorizationClientTestConfiguration.class, FeatureTestConfiguration.class })
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

        Mockito.when(service.get(Mockito.anyString())).thenReturn(detail);

        return service;
    }

    @Bean
    @Override
    public JndiObjectFactoryBean jndiObjectFactoryBean() {

        JndiObjectFactoryBean jndiObjectFactoryBean = super.jndiObjectFactoryBean();

        try {
            jndiObjectFactoryBean.setJndiTemplate(
                    new ExpectedLookupTemplate(JNDI_NAME, new ClassPathResource("exemple-ecommerce-authorization-test.yml").getURL().toString()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return jndiObjectFactoryBean;
    }
}
