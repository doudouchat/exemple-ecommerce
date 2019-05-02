package com.exemple.ecommerce.customer.core;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = { "com.exemple.ecommerce.customer" })
@ImportResource("classpath:exemple-ecommerce-customer.xml")
public class CustomerConfiguration {

}
