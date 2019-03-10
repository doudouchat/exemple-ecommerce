package com.exemple.ecommerce.resource.core;

import java.nio.ByteBuffer;

import javax.annotation.PostConstruct;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.QueryLogger;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.utils.Bytes;
import com.datastax.driver.extras.codecs.MappingCodec;
import com.datastax.driver.extras.codecs.jdk8.InstantCodec;
import com.datastax.driver.extras.codecs.jdk8.LocalDateCodec;
import com.datastax.driver.extras.codecs.json.JacksonJsonCodec;
import com.fasterxml.jackson.databind.JsonNode;

@Configuration
@EnableCaching
@EnableAspectJAutoProxy
@ComponentScan(basePackages = { "com.exemple.ecommerce.resource" })
@ImportResource("classpath:exemple-ecommerce-resource.xml")
public class ResourceConfiguration {

    @Value("${resource.cassandra.addresses}")
    private String[] addresses;

    @Value("${resource.cassandra.port}")
    private int port;

    @Bean(destroyMethod = "close")
    public Cluster cluster() {

        QueryLogger queryLogger = QueryLogger.builder().build();

        return Cluster.builder().withoutJMXReporting().addContactPoints(addresses).withPort(port).build().register(queryLogger);
    }

    @Bean
    public Session session() {

        return cluster().connect();
    }

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

    @PostConstruct
    public void init() {

        TypeCodec<JsonNode> jsonNodeCodec = new JacksonJsonCodec<>(JsonNode.class);

        cluster().getConfiguration().getCodecRegistry().register(LocalDateCodec.instance).register(InstantCodec.instance).register(jsonNodeCodec)
                .register(new ByteCodec());

    }

    private static class ByteCodec extends MappingCodec<byte[], ByteBuffer> {

        ByteCodec() {
            super(TypeCodec.blob(), byte[].class);
        }

        @Override
        protected byte[] deserialize(ByteBuffer value) {
            return value != null ? Bytes.getArray(value) : null;
        }

        @Override
        protected ByteBuffer serialize(byte[] value) {
            return value != null ? ByteBuffer.wrap(value) : null;
        }

    }

}
