package com.exemple.ecommerce.authorization.core.token;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import com.exemple.ecommerce.authorization.common.model.AccountUser;
import com.exemple.ecommerce.authorization.common.model.BackUser;

@Configuration
public class AuthorizationTokenConfiguration {

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("123");
        converter.setAccessTokenConverter(new CustomAccessTokenConverter());
        return converter;
    }

    @Bean
    public TokenEnhancer tokenEnhancer() {

        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList((OAuth2AccessToken accessToken, OAuth2Authentication authentication) -> {

            Map<String, Object> additionalInfo = new HashMap<>();

            if (authentication.getPrincipal() instanceof AccountUser) {

                AccountUser user = (AccountUser) authentication.getPrincipal();
                additionalInfo.put("id", user.getAccount());

            }

            ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);

            return accessToken;
        }, accessTokenConverter()));

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
