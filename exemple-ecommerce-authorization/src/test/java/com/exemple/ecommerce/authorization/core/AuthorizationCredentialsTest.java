package com.exemple.ecommerce.authorization.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;
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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.auth0.jwt.impl.JWTParser;
import com.auth0.jwt.interfaces.JWTPartsParser;
import com.auth0.jwt.interfaces.Payload;
import com.exemple.ecommerce.authorization.common.LoggingFilter;
import com.exemple.ecommerce.authorization.core.client.AuthorizationClientBuilder;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(classes = { AuthorizationTestConfiguration.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthorizationCredentialsTest extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationCredentialsTest.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AuthorizationClientBuilder authorizationClientBuilder;

    private String accessToken;

    private RequestSpecification requestSpecification;

    @BeforeClass
    private void init() throws Exception {

        String password = "{bcrypt}" + BCrypt.hashpw("secret", BCrypt.gensalt());

        authorizationClientBuilder

                .withClient("test").secret(password).authorizedGrantTypes("client_credentials").redirectUris("xxx").scopes("account:create")
                .autoApprove("account:create").authorities("ROLE_APP").resourceIds("app1")

                .and()

                .withClient("resource").secret(password).authorizedGrantTypes("client_credentials").authorities("ROLE_TRUSTED_CLIENT")

                .and().build();

    }

    @BeforeMethod
    private void before() {

        requestSpecification = RestAssured.given().filters(new LoggingFilter(LOG));

    }

    @Test
    public void credentialsSuccess() {

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "client_credentials");

        Response response = requestSpecification.auth().basic("test", "secret").formParams(params).post(restTemplate.getRootUri() + "/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        accessToken = response.jsonPath().getString("access_token");
        assertThat(accessToken, is(notNullValue()));

    }

    @DataProvider(name = "credentialsFailure")
    private static Object[][] credentialsFailure() {

        return new Object[][] {
                // bad password
                { "test", UUID.randomUUID().toString() },
                // bad login
                { UUID.randomUUID().toString(), "secret" }

        };
    }

    @Test(dataProvider = "credentialsFailure")
    public void credentialsFailure(String login, String password) {

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "client_credentials");

        Response response = requestSpecification.auth().basic(login, password).formParams(params).post(restTemplate.getRootUri() + "/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN.value()));

    }

    @Test(dependsOnMethods = "credentialsSuccess")
    public void checkToken() {

        Map<String, String> params = new HashMap<>();
        params.put("token", accessToken);

        Response response = requestSpecification.auth().basic("resource", "secret").formParams(params)
                .post(restTemplate.getRootUri() + "/oauth/check_token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        JWTPartsParser parser = new JWTParser();
        Payload payload = parser.parsePayload(response.getBody().print());

        assertThat(payload.getClaim("aud").asArray(String.class), arrayContainingInAnyOrder("app1"));
        assertThat(payload.getClaim("authorities").asArray(String.class), arrayContainingInAnyOrder("ROLE_APP"));
        assertThat(payload.getClaim("scope").asArray(String.class), arrayWithSize(1));
        assertThat(payload.getClaim("scope").asArray(String.class), arrayContainingInAnyOrder("account:create"));

    }

    @Test(dependsOnMethods = "credentialsSuccess")
    public void checkTokenFailure() {

        Map<String, String> params = new HashMap<>();
        params.put("token", accessToken);

        Response response = requestSpecification.auth().basic("test", "secret").formParams(params)
                .post(restTemplate.getRootUri() + "/oauth/check_token");

        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN.value()));

    }

}
