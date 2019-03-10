package com.exemple.ecommerce.schema.core;

import java.io.IOException;

import org.everit.json.schema.Schema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;

import com.exemple.ecommerce.schema.common.SchemaBuilder;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = { "com.exemple.ecommerce.schema" })
public class SchemaConfiguration {
    @Bean
    public Schema patchSchema() throws IOException {

        return SchemaBuilder.build(new ClassPathResource("json-patch.json").getInputStream());

    }

}
