package com.exemple.ecommerce.resource.core;

import javax.validation.Validator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import com.exemple.ecommerce.resource.core.cache.ResourceCacheConfiguration;
import com.exemple.ecommerce.resource.core.cassandra.ResourceCassandraConfiguration;

@Configuration
@EnableAspectJAutoProxy
@Import({ ResourceCassandraConfiguration.class, ResourceCacheConfiguration.class })
@ComponentScan(basePackages = { "com.exemple.ecommerce.resource" })
public class ResourceConfiguration {

    @Bean
    public Validator validator() {

        return new LocalValidatorFactoryBean();
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {

        MethodValidationPostProcessor methodValidationPostProcessor = new MethodValidationPostProcessor();
        methodValidationPostProcessor.setValidator(validator());

        return methodValidationPostProcessor;
    }

}
