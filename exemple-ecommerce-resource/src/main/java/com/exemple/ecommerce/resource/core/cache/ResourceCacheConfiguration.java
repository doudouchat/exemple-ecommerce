package com.exemple.ecommerce.resource.core.cache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@EnableCaching
@ImportResource("classpath:exemple-ecommerce-resource.xml")
public class ResourceCacheConfiguration {

}
