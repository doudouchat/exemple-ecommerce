package com.exemple.ecommerce.resource.core;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

import com.exemple.ecommerce.resource.core.cache.ResourceCacheConfiguration;
import com.exemple.ecommerce.resource.core.cassandra.ResourceCassandraConfiguration;

@Configuration
@EnableAspectJAutoProxy
@Import({ ResourceCassandraConfiguration.class, ResourceCacheConfiguration.class })
@ComponentScan(basePackages = "com.exemple.ecommerce.resource")
public class ResourceConfiguration {

}
