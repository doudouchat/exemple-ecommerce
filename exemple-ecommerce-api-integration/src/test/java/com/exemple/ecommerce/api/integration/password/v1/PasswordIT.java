package com.exemple.ecommerce.api.integration.password.v1;

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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT;
import com.exemple.ecommerce.api.integration.core.IntegrationTestConfiguration;
import com.exemple.ecommerce.api.integration.core.JsonRestTemplate;
import com.exemple.ecommerce.api.integration.login.v1.LoginIT;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class PasswordIT extends AbstractTestNGSpringContextTests {

    static String ACCESS_APP_TOKEN = null;

    static String ACCESS_TOKEN = null;

    @Test(dependsOnMethods = "com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.updateSuccess")
    public void connexion() {

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "client_credentials");

        Response response = JsonRestTemplate.given(JsonRestTemplate.AUTHORIZATION_URL, ContentType.URLENC).auth().basic("trusted_client", "secret")
                .formParams(params).post("/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        ACCESS_APP_TOKEN = response.jsonPath().getString("access_token");
        assertThat(ACCESS_APP_TOKEN, is(notNullValue()));

    }

    @Test(dependsOnMethods = "connexion")
    public void password() {

        Map<String, Object> newPassword = new HashMap<>();
        newPassword.put("login", AccountNominalIT.ACCOUNT_BODY.get("email"));

        Response response = JsonRestTemplate.given(JsonRestTemplate.AUTHORIZATION_URL, ContentType.JSON)

                .header(APP_HEADER, APP_HEADER_VALUE)

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(newPassword).post("/ws/v1/new_password");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        ACCESS_TOKEN = response.jsonPath().getString("token");
        assertThat(ACCESS_TOKEN, is(notNullValue()));

    }

    private String newPassword = UUID.randomUUID().toString();

    @Test(dependsOnMethods = "password")
    public void updateLogin() {

        List<Map<String, Object>> patchs = new ArrayList<>();

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "add");
        patch.put("path", "/password");
        patch.put("value", newPassword);

        patchs.add(patch);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_TOKEN).body(patchs)
                .patch(LoginIT.URL + "/{login}", AccountNominalIT.ACCOUNT_BODY.get("email"));

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test
    public void readLogin() {

        Map<String, Object> newPassword = new HashMap<>();
        newPassword.put("login", AccountNominalIT.ACCOUNT_BODY.get("email"));

        Response response = JsonRestTemplate.given(JsonRestTemplate.AUTHORIZATION_URL, ContentType.JSON)

                .header(APP_HEADER, APP_HEADER_VALUE)

                .header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .body(newPassword).post("/ws/v1/new_password");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        String accessToken = response.jsonPath().getString("token");
        assertThat(accessToken, is(notNullValue()));

        response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, VERSION_HEADER_VALUE)

                .header("Authorization", "Bearer " + accessToken).get(LoginIT.URL + "/{login}", AccountNominalIT.ACCOUNT_BODY.get("email"));

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(response.jsonPath().getString("login"), is(AccountNominalIT.ACCOUNT_BODY.get("email")));

    }

    @Test(dependsOnMethods = "updateLogin")
    public void connexionSuccess() {

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", AccountNominalIT.ACCOUNT_BODY.get("email"));
        params.put("password", newPassword);
        params.put("client_id", "test_user");
        params.put("redirect_uri", "xxx");

        Response response = JsonRestTemplate.given(JsonRestTemplate.AUTHORIZATION_URL, ContentType.URLENC).auth().basic("test_user", "secret")
                .formParams(params).post("/oauth/token");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(response.jsonPath().getString("access_token"), is(notNullValue()));

    }

    @Test(dependsOnMethods = "updateLogin")
    public void updateLoginForbidden() {

        List<Map<String, Object>> patchs = new ArrayList<>();

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "add");
        patch.put("path", "/password");
        patch.put("value", newPassword);

        patchs.add(patch);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .header("Authorization", "Bearer " + ACCESS_TOKEN).body(patchs)
                .patch(LoginIT.URL + "/{login}", AccountNominalIT.ACCOUNT_BODY.get("email"));

        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN.value()));

    }

}
