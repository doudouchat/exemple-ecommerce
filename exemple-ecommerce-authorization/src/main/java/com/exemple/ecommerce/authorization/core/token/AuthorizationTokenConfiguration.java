package com.exemple.ecommerce.authorization.core.token;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import com.auth0.jwt.impl.PublicClaims;
import com.exemple.ecommerce.authorization.common.model.AccountUser;
import com.exemple.ecommerce.authorization.common.model.BackUser;
import com.hazelcast.core.HazelcastInstance;

@Configuration
public class AuthorizationTokenConfiguration {

    public static final String TOKEN_BLACK_LIST = "token.black_list";

    @Value("${authorization.certificat.location}")
    private String location;

    @Value("${authorization.certificat.alias}")
    private String alias;

    @Value("${authorization.certificat.password}")
    private String password;

    private final ResourceLoader resourceLoader;

    private final HazelcastInstance hazelcastInstance;

    public AuthorizationTokenConfiguration(ResourceLoader resourceLoader, HazelcastInstance hazelcastInstance) {
        this.resourceLoader = resourceLoader;
        this.hazelcastInstance = hazelcastInstance;
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resourceLoader.getResource(location), password.toCharArray());
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair(alias));
        converter.setAccessTokenConverter(new CustomAccessTokenConverter());
        converter.setJwtClaimsSetVerifier((Map<String, Object> claims) -> {

            Object jti = claims.get(PublicClaims.JWT_ID);
            if (jti != null && hazelcastInstance.getMap(TOKEN_BLACK_LIST).containsKey(jti.toString())) {
                throw new InvalidTokenException(jti + " has been excluded");
            }
        });
        return converter;
    }

    @Bean
    public TokenEnhancer tokenEnhancer() {

        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Collections.singletonList(accessTokenConverter()));

        return tokenEnhancerChain;

    }

    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setSupportRefreshToken(true);
        return defaultTokenServices;
    }

    private static class CustomAccessTokenConverter extends DefaultAccessTokenConverter {

        @Override
        public Map<String, ?> convertAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {

            if (authentication.getPrincipal() instanceof AccountUser) {

                AccountUser user = (AccountUser) authentication.getPrincipal();

                ((DefaultOAuth2AccessToken) token)
                        .setScope(user.getScopes().stream().filter(scope -> token.getScope().contains(scope)).collect(Collectors.toSet()));
            }

            if (authentication.getPrincipal() instanceof BackUser) {

                BackUser user = (BackUser) authentication.getPrincipal();

                ((DefaultOAuth2AccessToken) token)
                        .setScope(user.getScopes().stream().filter(scope -> token.getScope().contains(scope)).collect(Collectors.toSet()));
            }

            return super.convertAccessToken(token, authentication);
        }
    }

}
