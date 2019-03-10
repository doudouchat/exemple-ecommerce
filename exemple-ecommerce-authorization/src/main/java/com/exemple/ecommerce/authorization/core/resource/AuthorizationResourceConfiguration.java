package com.exemple.ecommerce.authorization.core.resource;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.exemple.ecommerce.application.core.ApplicationConfiguration;
import com.exemple.ecommerce.resource.core.ResourceConfiguration;

@Configuration
@Import({ ResourceConfiguration.class, ApplicationConfiguration.class })
@ComponentScan(basePackageClasses = AuthorizationResourceConfiguration.class)
public class AuthorizationResourceConfiguration {

}
