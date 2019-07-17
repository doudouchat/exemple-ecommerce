package com.exemple.ecommerce.api.integration.subscription.v1;

import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.APP_HEADER;
import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.APP_HEADER_VALUE;
import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.VERSION_HEADER;
import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.VERSION_HEADER_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT;
import com.exemple.ecommerce.api.integration.core.JsonRestTemplate;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class SubscriptionIT {

    public static final String URL = "/ws/v1/subscriptions";

    private static final String EMAIL = UUID.randomUUID().toString() + "@gmail.com";

    static String ACCESS_TOKEN = null;

    @Test
    public void connexion() {

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "client_credentials");

        Response response = JsonRestTemplate.given(JsonRestTemplate.AUTHORIZATION_URL, ContentType.URLENC).auth().basic(APP_HEADER_VALUE, "secret")
                .formParams(params).post("/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        ACCESS_TOKEN = response.jsonPath().getString("access_token");
        assertThat(ACCESS_TOKEN, is(notNullValue()));

    }

    @Test(dependsOnMethods = "connexion")
    public void createSubscription() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .body(Collections.emptyMap()).put(URL + "/{email}", EMAIL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

    }

    @Test(dependsOnMethods = "createSubscription")
    public void readSubscription() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, VERSION_HEADER_VALUE)

                .header("Authorization", "Bearer " + ACCESS_TOKEN).get(URL + "/{email}", EMAIL);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(response.jsonPath().getString("email"), is(EMAIL));

    }

    @Test(dependsOnMethods = { "connexion", "com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.createSuccess" })
    public void createSubscriptionFailure() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_TOKEN)

                .body(Collections.emptyMap()).put(URL + "/{email}", AccountNominalIT.ACCOUNT_BODY.get("email"));

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.jsonPath().getList("code").get(0), is("login"));
        assertThat(response.jsonPath().getList("path").get(0), is("/email"));

    }
}
