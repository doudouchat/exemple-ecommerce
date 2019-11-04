package com.exemple.ecommerce.resource.core.cassandra;

import java.nio.ByteBuffer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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
@ComponentScan(basePackages = "com.exemple.ecommerce.resource.core.statement")
public class ResourceCassandraConfiguration {

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
