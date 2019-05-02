package com.exemple.ecommerce.authorization.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.auth0.jwt.impl.JWTParser;
import com.auth0.jwt.interfaces.JWTPartsParser;
import com.auth0.jwt.interfaces.Payload;
import com.exemple.ecommerce.authorization.common.LoggingFilter;
import com.exemple.ecommerce.authorization.core.client.AuthorizationClientBuilder;
import com.exemple.ecommerce.resource.login.LoginResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(classes = { AuthorizationTestConfiguration.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthorizationPasswordTest extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationPasswordTest.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private TestRestTemplate restTemplate;

    private String accessToken;

    private String accessTokenBack;

    private String login;

    @Autowired
    private LoginResource resource;

    @Autowired
    private AuthorizationClientBuilder authorizationClientBuilder;

    private RequestSpecification requestSpecification;

    @BeforeClass
    private void init() throws Exception {

        String password = "{bcrypt}" + BCrypt.hashpw("secret", BCrypt.gensalt());

        authorizationClientBuilder

                .withClient("test_user").secret(password).authorizedGrantTypes("password", "authorization_code", "refresh_token").redirectUris("xxx")
                .scopes("account:read", "account:update").autoApprove("account:read", "account:update").authorities("ROLE_APP").resourceIds("app1")

                .and()

                .withClient("back_user").secret(password).authorizedGrantTypes("password").scopes("stock:read", "stock:update")
                .autoApprove("stock:read", "stock:update").authorities("ROLE_BACK").resourceIds("app1")

                .and()

                .withClient("resource").secret(password).authorizedGrantTypes("client_credentials").authorities("ROLE_TRUSTED_CLIENT")

                .and().build();

    }

    @BeforeMethod
    private void before() {

        Mockito.reset(resource);

        requestSpecification = RestAssured.given().filters(new LoggingFilter(LOG));

    }

    @Test
    public void passwordSuccess() {

        login = "jean.dupond@gmail.com";

        Map<String, Object> account = new HashMap<>();
        account.put("login", login);
        account.put("password", "{bcrypt}" + BCrypt.hashpw("123", BCrypt.gensalt()));
        account.put("roles", new HashSet<>(Arrays.asList("ROLE_1", "ROLE_2")));
        account.put("scopes", new HashSet<>(Arrays.asList("account:read", "account:update")));

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.of(MAPPER.convertValue(account, JsonNode.class)));

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", login);
        params.put("password", "123");
        params.put("client_id", "test_user");
        params.put("redirect_uri", "xxx");

        Response response = requestSpecification.auth().basic("test_user", "secret").formParams(params)
                .post(restTemplate.getRootUri() + "/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        accessToken = response.jsonPath().getString("access_token");
        assertThat(accessToken, is(notNullValue()));

    }

    @Test
    public void passwordFailure() {

        String login = "jean.dupond@gmail.com";

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.empty());

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", login);
        params.put("password", "123");
        params.put("client_id", "test_user");
        params.put("redirect_uri", "xxx");

        Response response = requestSpecification.auth().basic("test_user", "secret").formParams(params)
                .post(restTemplate.getRootUri() + "/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED.value()));

        String error = response.jsonPath().getString("error");
        assertThat(error, is("unauthorized"));

    }

    @Test(dependsOnMethods = "passwordSuccess")
    public void checkToken() {

        Map<String, String> params = new HashMap<>();
        params.put("token", accessToken);

        Response response = requestSpecification.auth().basic("resource", "secret").formParams(params)
                .post(restTemplate.getRootUri() + "/oauth/check_token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        JWTPartsParser parser = new JWTParser();
        Payload payload = parser.parsePayload(response.getBody().print());

        assertThat(payload.getClaim("user_name").asString(), is(this.login));
        assertThat(payload.getClaim("aud").asArray(String.class), arrayContainingInAnyOrder("app1"));
        assertThat(payload.getClaim("authorities").asArray(String.class), arrayContainingInAnyOrder("ROLE_2", "ROLE_1"));
        assertThat(payload.getClaim("scope").asArray(String.class), arrayWithSize(2));
        assertThat(payload.getClaim("scope").asArray(String.class), arrayContainingInAnyOrder("account:read", "account:update"));

    }

    @Test
    public void passwordBackSuccess() {

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", "admin");
        params.put("password", "admin123");
        params.put("client_id", "back_user");
        params.put("redirect_uri", "xxx");

        Response response = requestSpecification.auth().basic("back_user", "secret").formParams(params)
                .post(restTemplate.getRootUri() + "/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        accessTokenBack = response.jsonPath().getString("access_token");
        assertThat(accessTokenBack, is(notNullValue()));

    }

    @Test
    public void passwordBackFailure() {

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", "bad_login");
        params.put("password", "admin123");
        params.put("client_id", "back_user");
        params.put("redirect_uri", "xxx");

        Response response = requestSpecification.auth().basic("back_user", "secret").formParams(params)
                .post(restTemplate.getRootUri() + "/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED.value()));

        String error = response.jsonPath().getString("error");
        assertThat(error, is("unauthorized"));

    }

    @Test(dependsOnMethods = "passwordBackSuccess")
    public void checkBackToken() {

        Map<String, String> params = new HashMap<>();
        params.put("token", accessTokenBack);

        Response response = requestSpecification.auth().basic("resource", "secret").formParams(params)
                .post(restTemplate.getRootUri() + "/oauth/check_token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        JWTPartsParser parser = new JWTParser();
        Payload payload = parser.parsePayload(response.getBody().print());

        assertThat(payload.getClaim("aud").asArray(String.class), arrayContainingInAnyOrder("app1"));
        assertThat(payload.getClaim("authorities").asArray(String.class), arrayContainingInAnyOrder("ROLE_STOCK"));
        assertThat(payload.getClaim("scope").asArray(String.class), arrayWithSize(2));
        assertThat(payload.getClaim("scope").asArray(String.class), arrayContainingInAnyOrder("stock:read", "stock:update"));

    }

}
