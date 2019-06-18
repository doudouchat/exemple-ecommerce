package com.exemple.ecommerce.api.integration.login.v1;

import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.ACCESS_APP_TOKEN;
import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.APP_HEADER;
import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.APP_HEADER_VALUE;
import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.VERSION_HEADER;
import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.VERSION_HEADER_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.exemple.ecommerce.api.integration.core.JsonRestTemplate;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class LoginIT {

    public static final String URL = "/ws/v1/login";

    private static final String LOGIN = UUID.randomUUID() + "@gmail.com";

    private static String ACCESS_TOKEN = null;

    @Test(dependsOnMethods = "com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.connexion")
    public void create() {

        Map<String, Object> body = new HashMap<>();
        body.put("login", LOGIN);
        body.put("password", "mdp");
        body.put("id", UUID.randomUUID());

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, VERSION_HEADER_VALUE)

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(body).post(LoginIT.URL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

    }

    @Test(dependsOnMethods = "create")
    public void exist() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .head(URL + "/{login}", LOGIN);
        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "exist")
    public void connexionSuccess() {

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", LOGIN);
        params.put("password", "mdp");
        params.put("client_id", "test_user");
        params.put("redirect_uri", "xxx");

        Response response = JsonRestTemplate.given(JsonRestTemplate.AUTHORIZATION_URL, ContentType.URLENC).auth().basic("test_user", "secret")
                .formParams(params).post("/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        ACCESS_TOKEN = response.jsonPath().getString("access_token");
        assertThat(ACCESS_TOKEN, is(notNullValue()));

    }

    @Test(dependsOnMethods = "connexionSuccess")
    public void update() {

        List<Map<String, Object>> patchs = new ArrayList<>();

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "add");
        patch.put("path", "/password");
        patch.put("value", "new_mdp");

        patchs.add(patch);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_TOKEN).body(patchs).patch(LoginIT.URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "connexionSuccess")
    public void updateFailure() {

        List<Map<String, Object>> patchs = new ArrayList<>();

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "replace");
        patch.put("path", "/id");
        patch.put("value", UUID.randomUUID());

        patchs.add(patch);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_TOKEN).body(patchs).patch(LoginIT.URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST.value()));

    }

    @Test(dependsOnMethods = { "update", "updateFailure" })
    public void delete() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header("Authorization", "Bearer " + ACCESS_TOKEN)

                .delete(URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "delete")
    public void notFound() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .head(URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND.value()));

    }
}
