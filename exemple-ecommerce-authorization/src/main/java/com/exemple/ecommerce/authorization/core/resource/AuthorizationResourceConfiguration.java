package com.exemple.ecommerce.authorization.core.resource;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.exemple.ecommerce.application.core.ApplicationConfiguration;
import com.exemple.ecommerce.resource.core.cassandra.ResourceCassandraConfiguration;

@Configuration
@Import({ ResourceCassandraConfiguration.class, ApplicationConfiguration.class })
@ComponentScan(basePackages = { "com.exemple.ecommerce.resource.login", "com.exemple.ecommerce.resource.account" })
public class AuthorizationResourceConfiguration {

}
