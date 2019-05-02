package com.exemple.ecommerce.authorization.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
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
public class AuthorizationCodeTest extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationCodeTest.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AuthorizationClientBuilder authorizationClientBuilder;

    @Autowired
    private Algorithm algorithm;

    private String xAuthToken;

    private String location;

    private String accessToken;

    private String login;

    @Autowired
    private LoginResource resource;

    private RequestSpecification requestSpecification;

    @BeforeClass
    private void init() throws Exception {

        String password = "{bcrypt}" + BCrypt.hashpw("secret", BCrypt.gensalt());

        authorizationClientBuilder

                .withClient("test").secret(password).authorizedGrantTypes("client_credentials").redirectUris("xxx").scopes("account:create")
                .autoApprove("account:create").authorities("ROLE_APP").resourceIds("app1")

                .and()

                .withClient("test_user").secret(password).authorizedGrantTypes("password", "authorization_code", "refresh_token").redirectUris("xxx")
                .scopes("account:read", "account:update").autoApprove("account:read", "account:update").authorities("ROLE_APP").resourceIds("app1")

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
    public void credentials() {

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "client_credentials");

        Response response = requestSpecification.auth().basic("test", "secret").formParams(params).post(restTemplate.getRootUri() + "/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        accessToken = response.jsonPath().getString("access_token");
        assertThat(accessToken, is(notNullValue()));

    }

    @Test(dependsOnMethods = "credentials")
    public void login() {

        login = "jean.dupond@gmail.com";

        Map<String, Object> account = new HashMap<>();
        account.put("login", login);
        account.put("password", "{bcrypt}" + BCrypt.hashpw("123", BCrypt.gensalt()));
        account.put("roles", Collections.singleton("ROLE_ACCOUNT"));
        account.put("scopes", new HashSet<>(Arrays.asList("account:read", "account:update")));

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.of(MAPPER.convertValue(account, JsonNode.class)));

        Response response = requestSpecification.header("Authorization", "Bearer " + accessToken).formParams("username", login, "password", "123")
                .post(restTemplate.getRootUri() + "/login");
        xAuthToken = response.getHeader("X-Auth-Token");

        assertThat(response.getStatusCode(), is(HttpStatus.FOUND.value()));
        assertThat(xAuthToken, is(notNullValue()));
        assertThat(response.getCookies().isEmpty(), is(true));
    }

    @Test(dependsOnMethods = "login")
    public void authorize() {

        String authorizeUrl = restTemplate.getRootUri() + "/oauth/authorize?response_type=code&client_id=" + "test_user" + "&scope=account:read";
        Response response = requestSpecification.header("X-Auth-Token", xAuthToken).post(authorizeUrl);
        location = response.getHeader(HttpHeaders.LOCATION);

        assertThat(response.getStatusCode(), is(HttpStatus.FOUND.value()));
        assertThat(location, is(notNullValue()));
    }

    @Test(dependsOnMethods = "authorize")
    public void token() {

        String code = location.substring(location.indexOf("code=") + 5);

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("client_id", "test_user");
        params.put("redirect_uri", "xxx");

        Response response = requestSpecification.auth().basic("test_user", "secret").formParams(params)
                .post(restTemplate.getRootUri() + "/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        accessToken = response.jsonPath().getString("access_token");
        assertThat(accessToken, is(notNullValue()));

    }

    @Test(dependsOnMethods = "token")
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
        assertThat(payload.getClaim("authorities").asArray(String.class), arrayContainingInAnyOrder("ROLE_ACCOUNT"));
        assertThat(payload.getClaim("scope").asArray(String.class), arrayWithSize(1));
        assertThat(payload.getClaim("scope").asArray(String.class), arrayContainingInAnyOrder("account:read"));

    }

    @DataProvider(name = "loginFailure")
    private Object[][] loginFailure() {

        String accessToken1 = JWT.create().withArrayClaim("authorities", new String[] { "ROLE_ACCOUNT" }).sign(algorithm);
        String accessToken2 = JWT.create().withExpiresAt(new Date(Instant.now().minus(1, ChronoUnit.HOURS).toEpochMilli()))
                .withArrayClaim("authorities", new String[] { "ROLE_APP" }).sign(algorithm);
        String accessToken3 = JWT.create().withArrayClaim("authorities", new String[] { "ROLE_APP" }).sign(algorithm);
        String accessToken4 = JWT.create().sign(algorithm);

        return new Object[][] {
                // not authorities to access
                { "Authorization", "Bearer " + accessToken1 },
                // token is expired
                { "Authorization", "Bearer " + accessToken2 },
                // not bearer
                { "Authorization", accessToken3 },
                // not token
                { "Header", "Bearer " + accessToken3 },
                // auhorities is empty
                { "Authorization", "Bearer " + accessToken4 },
                // token no recognized
                { "Authorization", "Bearer toto" }

        };
    }

    @Test(dataProvider = "loginFailure")
    public void loginFailure(String header, String headerValue) {

        String login = "jean.dupond@gmail.com";

        Map<String, Object> account = new HashMap<>();
        account.put("login", login);
        account.put("password", "{bcrypt}" + BCrypt.hashpw("123", BCrypt.gensalt()));
        account.put("roles", Collections.singleton("ROLE_ACCOUNT"));

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.of(MAPPER.convertValue(account, JsonNode.class)));

        Response response = requestSpecification.header(header, headerValue).formParams("username", login, "password", "123")
                .post(restTemplate.getRootUri() + "/login");

        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN.value()));

    }

}
