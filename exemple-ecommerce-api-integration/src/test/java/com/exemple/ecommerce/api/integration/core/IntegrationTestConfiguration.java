package com.exemple.ecommerce.api.integration.core;

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

import com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT;
import com.exemple.ecommerce.api.integration.password.v1.PasswordIT;
import com.exemple.ecommerce.api.integration.stock.v1.StockNominalIT;
import com.exemple.ecommerce.application.common.model.ApplicationDetail;
import com.exemple.ecommerce.application.core.ApplicationConfiguration;
import com.exemple.ecommerce.application.detail.ApplicationDetailService;
import com.exemple.ecommerce.authorization.core.client.AuthorizationClientBuilder;
import com.exemple.ecommerce.authorization.core.client.AuthorizationClientConfiguration;
import com.exemple.ecommerce.resource.common.model.ResourceSchema;
import com.exemple.ecommerce.resource.core.ResourceConfiguration;
import com.exemple.ecommerce.resource.core.ResourceExecutionContext;
import com.exemple.ecommerce.resource.schema.SchemaResource;
import com.google.common.collect.Sets;

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

        // APP

        ApplicationDetail detail = new ApplicationDetail();
        detail.setKeyspace("test");
        detail.setCompany("test_company");
        detail.setClientIds(Sets.newHashSet("test", "test_user"));

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
        Map<String, Set<String>> accountRules = new HashMap<>();
        accountRules.put("login", Collections.singleton("/email"));
        accountRules.put("maxProperties", Collections.singleton("/addresses,2"));
        accountRules.put("dependencies", Collections.singleton("optin_mobile,mobile"));

        ResourceExecutionContext.get().setKeyspace(detail.getKeyspace());

        ResourceSchema accountSchema = new ResourceSchema();
        accountSchema.setApplication(AccountNominalIT.APP_HEADER_VALUE);
        accountSchema.setVersion(AccountNominalIT.VERSION_HEADER_VALUE);
        accountSchema.setResource("account");
        accountSchema.setContent(IOUtils.toByteArray(new ClassPathResource("account.json").getInputStream()));
        accountSchema.setFilters(accountFilter);
        accountSchema.setTransforms(dateTime);
        accountSchema.setRules(accountRules);

        schemaResource.save(accountSchema);

        Set<String> loginFilter = new HashSet<>();
        loginFilter.add("id");
        loginFilter.add("enable");
        loginFilter.add("login");
        Map<String, Set<String>> loginRules = new HashMap<>();
        loginRules.put("login", Collections.singleton("/login"));
        loginRules.put("createOnly", Collections.singleton("/id"));

        ResourceSchema loginSchema = new ResourceSchema();
        loginSchema.setApplication(AccountNominalIT.APP_HEADER_VALUE);
        loginSchema.setVersion(AccountNominalIT.VERSION_HEADER_VALUE);
        loginSchema.setResource("login");
        loginSchema.setContent(IOUtils.toByteArray(new ClassPathResource("login.json").getInputStream()));
        loginSchema.setFilters(loginFilter);
        loginSchema.setTransforms(Collections.emptyMap());
        loginSchema.setRules(loginRules);

        schemaResource.save(loginSchema);

        Set<String> subscriptionFilter = new HashSet<>();
        subscriptionFilter.add("email");

        Map<String, Set<String>> subscriptionRules = new HashMap<>();
        subscriptionRules.put("login", Collections.singleton("/email"));

        ResourceSchema subscriptionSchema = new ResourceSchema();
        subscriptionSchema.setApplication(AccountNominalIT.APP_HEADER_VALUE);
        subscriptionSchema.setVersion(AccountNominalIT.VERSION_HEADER_VALUE);
        subscriptionSchema.setResource("subscription");
        subscriptionSchema.setContent(IOUtils.toByteArray(new ClassPathResource("subscription.json").getInputStream()));
        subscriptionSchema.setFilters(subscriptionFilter);
        subscriptionSchema.setTransforms(Collections.emptyMap());
        subscriptionSchema.setRules(subscriptionRules);

        schemaResource.save(subscriptionSchema);

        applicationDetailService.put(AccountNominalIT.APP_HEADER_VALUE, detail);

        // STOCK

        ApplicationDetail backDetail = new ApplicationDetail();
        backDetail.setKeyspace("test");
        backDetail.setCompany("test_company");
        backDetail.setClientIds(Sets.newHashSet("back", "back_user"));

        applicationDetailService.put(StockNominalIT.APP_HEADER_VALUE, backDetail);

        // ADMIN

        loginSchema = new ResourceSchema();
        loginSchema.setApplication(PasswordIT.APP_HEADER_VALUE);
        loginSchema.setVersion(PasswordIT.VERSION_HEADER_VALUE);
        loginSchema.setResource("login");
        loginSchema.setContent(IOUtils.toByteArray(new ClassPathResource("login.json").getInputStream()));
        loginSchema.setFilters(loginFilter);
        loginSchema.setTransforms(Collections.emptyMap());
        loginSchema.setRules(loginRules);

        schemaResource.save(loginSchema);

        ApplicationDetail adminDetail = new ApplicationDetail();
        adminDetail.setKeyspace("test");
        adminDetail.setCompany("test_company");
        adminDetail.setClientIds(Sets.newHashSet("admin"));

        applicationDetailService.put(PasswordIT.APP_HEADER_VALUE, adminDetail);

    }

    @PostConstruct
    public void initAuthorization() throws Exception {

        String password = "{bcrypt}" + BCrypt.hashpw("secret", BCrypt.gensalt());

        authorizationClientBuilder

                .withClient("test").secret(password).authorizedGrantTypes("client_credentials").redirectUris("xxx")
                .scopes("account:create", "login:head", "login:create", "subscription:update", "subscription:read")
                .autoApprove("account:create", "login:create", "subscription:update", "subscription:read").authorities("ROLE_APP")
                .resourceIds("exemple").additionalInformation("keyspace=test")

                .and()

                .withClient("test_user").secret(password).authorizedGrantTypes("password", "authorization_code", "refresh_token").redirectUris("xxx")
                .scopes("account:read", "account:update", "login:update", "login:delete")
                .autoApprove("account:read", "account:update", "login:update", "login:delete").authorities("ROLE_APP").resourceIds("exemple")
                .additionalInformation("keyspace=test")

                .and()

                .withClient("back").secret(password).authorizedGrantTypes("client_credentials").scopes("stock").autoApprove("stock")
                .authorities("ROLE_BACK").resourceIds("exemple").additionalInformation("keyspace=test")

                .and()

                .withClient("back_user").secret(password).authorizedGrantTypes("password").scopes("stock:read", "stock:update")
                .autoApprove("stock:read", "stock:update").authorities("ROLE_BACK").resourceIds("exemple").additionalInformation("keyspace=test")

                .and()

                .withClient("resource").secret(password).authorizedGrantTypes("client_credentials").authorities("ROLE_TRUSTED_CLIENT")

                .and()

                .withClient("admin").secret(password).authorizedGrantTypes("client_credentials").scopes("xxx").autoApprove("xxx")
                .authorities("ROLE_TRUSTED_CLIENT").resourceIds("exemple").additionalInformation("keyspace=test")

                .and().build();
    }

}
