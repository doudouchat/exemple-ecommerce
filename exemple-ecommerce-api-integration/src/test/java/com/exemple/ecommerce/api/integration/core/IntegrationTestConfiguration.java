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

        Set<String> filter = new HashSet<>();
        filter.add("lastname");
        filter.add("firstname");
        filter.add("email");
        filter.add("optin_mobile");
        filter.add("civility");
        filter.add("mobile");
        filter.add("creation_date");
        filter.add("birthday");
        filter.add("addresses[*[city,street]]");
        filter.add("cgus[code,version]");
        Map<String, Set<String>> dateTime = Collections.singletonMap("date_time", Collections.singleton("creation_date"));
        Map<String, Set<String>> rules = new HashMap<>();
        rules.put("unique", Collections.singleton("/email"));
        rules.put("maxProperties", Collections.singleton("/addresses,2"));
        rules.put("dependencies", Collections.singleton("optin_mobile,mobile"));

        ResourceExecutionContext.get().setKeyspace(detail.getKeyspace());

        ResourceSchema resourceSchema = new ResourceSchema();
        resourceSchema.setApplication(APP_HEADER_VALUE);
        resourceSchema.setVersion(VERSION_HEADER_VALUE);
        resourceSchema.setResource("account");
        resourceSchema.setContent(IOUtils.toByteArray(new ClassPathResource("account.json").getInputStream()));
        resourceSchema.setFilters(filter);
        resourceSchema.setTransforms(dateTime);
        resourceSchema.setRules(rules);

        schemaResource.save(resourceSchema);

        applicationDetailService.put(APP_HEADER_VALUE, detail);

    }

    @PostConstruct
    public void initAuthorization() throws Exception {

        String password = "{bcrypt}" + BCrypt.hashpw("secret", BCrypt.gensalt());

        authorizationClientBuilder

                .withClient("test").secret(password).authorizedGrantTypes("client_credentials").redirectUris("xxx").scopes("account:create")
                .autoApprove("account:create").authorities("ROLE_APP").resourceIds(APP_HEADER_VALUE)

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

                .and().build();
    }

}
