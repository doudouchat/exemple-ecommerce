package com.exemple.ecommerce.api.integration.stock.v1;

import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.APP_HEADER;
import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.APP_HEADER_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.ecommerce.api.integration.core.IntegrationTestConfiguration;
import com.exemple.ecommerce.api.integration.core.JsonRestTemplate;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class StockNominalIT extends AbstractTestNGSpringContextTests {

    private static final String STOCK_URL = "/ws/v1/stock/{store}/{product}";

    private final String product = "product#" + UUID.randomUUID();

    private final String store = "store#" + UUID.randomUUID();

    public static String ACCESS_APP_TOKEN = null;

    public static String ACCESS_BACK_TOKEN = null;

    @Test
    public void connexion() {

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "client_credentials");

        Response response = JsonRestTemplate.given(JsonRestTemplate.AUTHORIZATION_URL, ContentType.URLENC).auth().basic("back", "secret")
                .formParams(params).post("/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        ACCESS_APP_TOKEN = response.jsonPath().getString("access_token");
        assertThat(ACCESS_APP_TOKEN, is(notNullValue()));

    }

    @Test(dependsOnMethods = "connexion")
    public void connexionBack() {

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", "admin");
        params.put("password", "admin123");
        params.put("client_id", "back_user");
        params.put("redirect_uri", "xxx");

        Response response = JsonRestTemplate.given(JsonRestTemplate.AUTHORIZATION_URL, ContentType.URLENC).auth().basic("back_user", "secret")
                .formParams(params).post("/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        ACCESS_BACK_TOKEN = response.jsonPath().getString("access_token");
        assertThat(ACCESS_BACK_TOKEN, is(notNullValue()));

    }

    @DataProvider(name = "updateSuccess")
    private static Object[][] updateSuccess() {

        return new Object[][] {

                { 5 },

                { 8 }

        };
    }

    @Test(dataProvider = "updateSuccess", dependsOnMethods = "connexionBack")
    public void updateSuccess(int increment) {

        Response response = JsonRestTemplate.given().body(Collections.singletonMap("increment", increment))

                .header("Authorization", "Bearer " + ACCESS_BACK_TOKEN).header(APP_HEADER, APP_HEADER_VALUE)

                .post(STOCK_URL, store, product);
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test(dependsOnMethods = "updateSuccess")
    public void getSuccess() {

        Response response = JsonRestTemplate.given()

                .header("Authorization", "Bearer " + ACCESS_BACK_TOKEN).header(APP_HEADER, APP_HEADER_VALUE)

                .get(STOCK_URL, store, product);

        assertThat(response.jsonPath().getInt("amount"), is(13));

    }

}
