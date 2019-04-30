package com.exemple.ecommerce.api.integration.core;

import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.APP_HEADER_VALUE;
import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.VERSION_HEADER_VALUE;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.exemple.ecommerce.application.common.model.ApplicationDetail;
import com.exemple.ecommerce.application.core.ApplicationConfiguration;
import com.exemple.ecommerce.application.detail.ApplicationDetailService;
import com.exemple.ecommerce.authorization.core.client.AuthorizationClientBuilder;
import com.exemple.ecommerce.authorization.core.client.AuthorizationClientConfiguration;
import com.exemple.ecommerce.resource.common.model.ResourceSchema;
import com.exemple.ecommerce.resource.core.ResourceConfiguration;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.exemple.ecommerce.resource.schema.SchemaResource;

@Configuration
@Import({ ResourceConfiguration.class, ApplicationConfiguration.class, AuthorizationClientConfiguration.class })
public class IntegrationTestConfiguration {

    @Autowired
    private SchemaResource schemaResource;

    @Autowired
    private ApplicationDetailService applicationDetailService;

    @Autowired
    private AuthorizationClientBuilder authorizationClientBuilder;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {

        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();

        YamlPropertiesFactoryBean properties = new YamlPropertiesFactoryBean();
        properties.setResources(new ClassPathResource("exemple-ecommerce-test.yml"));

        propertySourcesPlaceholderConfigurer.setProperties(properties.getObject());
        return propertySourcesPlaceholderConfigurer;
    }

    @PostConstruct
    public void initSchema() throws IOException {

        ApplicationDetail detail = new ApplicationDetail();
        detail.setKeyspace("test");
        detail.setCompany("test_company");

        Set<String> accountFilter = new HashSet<>();
        accountFilter.add("id");
        accountFilter.add("lastname");
        accountFilter.add("firstname");
        accountFilter.add("email");
        accountFilter.add("optin_mobile");
        accountFilter.add("civility");
        accountFilter.add("mobile");
        accountFilter.add("creation_date");
        accountFilter.add("birthday");
        accountFilter.add("addresses[*[city,street]]");
        accountFilter.add("cgus[code,version]");
        Map<String, Set<String>> dateTime = Collections.singletonMap("date_time", Collections.singleton("creation_date"));
        Map<String, Set<String>> rules = new HashMap<>();
        rules.put("unique", Collections.singleton("/email"));
        rules.put("maxProperties", Collections.singleton("/addresses,2"));
        rules.put("dependencies", Collections.singleton("optin_mobile,mobile"));

        ResourceExecutionContext.get().setKeyspace(detail.getKeyspace());

        ResourceSchema accountSchema = new ResourceSchema();
        accountSchema.setApplication(APP_HEADER_VALUE);
        accountSchema.setVersion(VERSION_HEADER_VALUE);
        accountSchema.setResource("account");
        accountSchema.setContent(IOUtils.toByteArray(new ClassPathResource("account.json").getInputStream()));
        accountSchema.setFilters(accountFilter);
        accountSchema.setTransforms(dateTime);
        accountSchema.setRules(rules);

        schemaResource.save(accountSchema);

        Set<String> loginFilter = new HashSet<>();
        loginFilter.add("id");
        loginFilter.add("enable");
        loginFilter.add("login");

        ResourceSchema loginSchema = new ResourceSchema();
        loginSchema.setApplication(APP_HEADER_VALUE);
        loginSchema.setVersion(VERSION_HEADER_VALUE);
        loginSchema.setResource("login");
        loginSchema.setContent(IOUtils.toByteArray(new ClassPathResource("login.json").getInputStream()));
        loginSchema.setFilters(loginFilter);
        loginSchema.setTransforms(Collections.emptyMap());
        loginSchema.setRules(Collections.emptyMap());

        schemaResource.save(loginSchema);

        Set<String> subscriptionFilter = new HashSet<>();
        subscriptionFilter.add("email");

        Map<String, Set<String>> subscriptionRules = new HashMap<>();
        subscriptionRules.put("unique", Collections.singleton("/email"));

        ResourceSchema subscriptionSchema = new ResourceSchema();
        subscriptionSchema.setApplication(APP_HEADER_VALUE);
        subscriptionSchema.setVersion(VERSION_HEADER_VALUE);
        subscriptionSchema.setResource("subscription");
        subscriptionSchema.setContent(IOUtils.toByteArray(new ClassPathResource("subscription.json").getInputStream()));
        subscriptionSchema.setFilters(subscriptionFilter);
        subscriptionSchema.setTransforms(Collections.emptyMap());
        subscriptionSchema.setRules(subscriptionRules);

        schemaResource.save(subscriptionSchema);

        applicationDetailService.put(APP_HEADER_VALUE, detail);

    }

    @PostConstruct
    public void initAuthorization() throws Exception {

        String password = "{bcrypt}" + BCrypt.hashpw("secret", BCrypt.gensalt());

        authorizationClientBuilder

                .withClient("test").secret(password).authorizedGrantTypes("client_credentials").redirectUris("xxx")
                .scopes("account:create", "subscription:update", "subscription:read")
                .autoApprove("account:create", "subscription:update", "subscription:read").authorities("ROLE_APP").resourceIds(APP_HEADER_VALUE)

                .and()

                .withClient("test_user").secret(password).authorizedGrantTypes("password", "authorization_code", "refresh_token").redirectUris("xxx")
                .scopes("account:read", "account:update").autoApprove("account:read", "account:update").authorities("ROLE_APP")
                .resourceIds(APP_HEADER_VALUE)

                .and()

                .withClient("back").secret(password).authorizedGrantTypes("client_credentials").scopes("stock").autoApprove("stock")
                .authorities("ROLE_BACK").resourceIds(APP_HEADER_VALUE)

                .and()

                .withClient("back_user").secret(password).authorizedGrantTypes("password").scopes("stock:read", "stock:update")
                .autoApprove("stock:read", "stock:update").authorities("ROLE_BACK").resourceIds(APP_HEADER_VALUE)

                .and()

                .withClient("resource").secret(password).authorizedGrantTypes("client_credentials").authorities("ROLE_TRUSTED_CLIENT")

                .and()

                .withClient("trusted_client").secret(password).authorizedGrantTypes("client_credentials").scopes("xxx").autoApprove("xxx")
                .authorities("ROLE_TRUSTED_CLIENT").resourceIds(APP_HEADER_VALUE)

                .and().build();
    }

}
