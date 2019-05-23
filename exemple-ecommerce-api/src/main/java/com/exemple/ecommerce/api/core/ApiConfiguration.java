package com.exemple.ecommerce.api.core;

import java.io.FileNotFoundException;
import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.UrlResource;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import com.exemple.ecommerce.api.core.authorization.AuthorizationConfiguration;

@Configuration
@Import(AuthorizationConfiguration.class)
@ComponentScan(basePackages = { "com.exemple.ecommerce.api" })
@ImportResource("classpath:exemple-ecommerce-api-security.xml")
public class ApiConfiguration {

    public static final String JNDI_NAME = "java:comp/env/exemple-ecommerce-configuration";

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
