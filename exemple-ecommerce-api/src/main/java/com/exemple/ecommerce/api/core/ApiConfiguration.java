package com.exemple.ecommerce.api.core;

import java.io.FileNotFoundException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.UrlResource;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

@Configuration
@ComponentScan(basePackages = { "com.exemple.ecommerce.api" })
@ImportResource("classpath:exemple-ecommerce-api-security.xml")
public class ApiConfiguration {

    public static final String JNDI_NAME = "java:comp/env/exemple-ecommerce-configuration";

    public static final String AVAILABLE_TOKENS = "api.available.tokens";

    @Value("${api.hazelcast.port}")
    private int port;

    @Bean
    public MessageSource messageSource() {

        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

        messageSource.setCacheSeconds(0);
        messageSource.setBasename("classpath:messages/erreur_messages");

        return messageSource;
    }

    @Bean
    public JndiObjectFactoryBean jndiObjectFactoryBean() {

        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName(JNDI_NAME);
        jndiObjectFactoryBean.setExpectedType(String.class);

        return jndiObjectFactoryBean;
    }

    @Bean
    @Profile("!noSecurity")
    public HazelcastInstance hazelcastInstance() {

        Config config = new Config();
        config.getNetworkConfig().setPort(port);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
        config.getMapConfig(AVAILABLE_TOKENS);

        return Hazelcast.newHazelcastInstance(config);
    }

    @Bean
    public PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() throws FileNotFoundException {

        JndiObjectFactoryBean jndiObjectFactoryBean = this.jndiObjectFactoryBean();

        YamlPropertiesFactoryBean propertiesFactoryBean = new YamlPropertiesFactoryBean();
        String resource = (String) jndiObjectFactoryBean.getObject();
        Assert.notNull(resource, jndiObjectFactoryBean.getJndiName() + " is required");
        propertiesFactoryBean.setResources(new UrlResource(ResourceUtils.getURL(resource)));

        Properties properties = propertiesFactoryBean.getObject();
        Assert.notNull(properties, jndiObjectFactoryBean.getJndiName() + " is required");

        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setProperties(properties);

        return propertySourcesPlaceholderConfigurer;
    }

}
