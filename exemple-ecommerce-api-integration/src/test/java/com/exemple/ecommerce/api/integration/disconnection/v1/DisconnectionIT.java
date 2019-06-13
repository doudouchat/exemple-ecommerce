package com.exemple.ecommerce.api.integration.disconnection.v1;

import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.ACCOUNT_URL;
import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.APP_HEADER;
import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.APP_HEADER_VALUE;
import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.ID;
import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.VERSION_HEADER;
import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.VERSION_HEADER_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT;
import com.exemple.ecommerce.api.integration.core.IntegrationTestConfiguration;
import com.exemple.ecommerce.api.integration.core.JsonRestTemplate;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class DisconnectionIT extends AbstractTestNGSpringContextTests {

    static String ACCESS_TOKEN = null;

    @Test(dependsOnMethods = "com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.updateSuccess")
    public void connection() {

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", AccountNominalIT.ACCOUNT_BODY.get("email"));
        params.put("password", AccountNominalIT.LOGIN_BODY.get("password"));
        params.put("client_id", "test_user");
        params.put("redirect_uri", "xxx");

        Response response = JsonRestTemplate.given(JsonRestTemplate.AUTHORIZATION_URL, ContentType.URLENC).auth().basic("test_user", "secret")
                .formParams(params).post("/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        ACCESS_TOKEN = response.jsonPath().getString("access_token");
        assertThat(ACCESS_TOKEN, is(notNullValue()));

        response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, VERSION_HEADER_VALUE)

                .header("Authorization", "Bearer " + ACCESS_TOKEN).get(ACCOUNT_URL + "/{id}", ID);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

    }

    @Test(dependsOnMethods = "connection")
    public void disconnection() {

        Response response = JsonRestTemplate.given(JsonRestTemplate.AUTHORIZATION_URL, ContentType.JSON)

                .header(APP_HEADER, APP_HEADER_VALUE)

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .post("/ws/v1/disconnection");

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "disconnection")
    public void getFailure() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, VERSION_HEADER_VALUE)

                .header("Authorization", "Bearer " + ACCESS_TOKEN).get(ACCOUNT_URL + "/{id}", ID);

        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN.value()));
    }

}
