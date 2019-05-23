package com.exemple.ecommerce.api.core.authorization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

@Configuration
public class AuthorizationTestConfiguration extends AuthorizationConfiguration {

    @Value("${api.authorization.hazelcast.port}")
    private int port;

    @Bean
    @Primary
    public HazelcastInstance hazelcastInstance() {

        Config config = new Config();
        config.getNetworkConfig().setPort(port);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);

        return Hazelcast.newHazelcastInstance(config);
    }
}
