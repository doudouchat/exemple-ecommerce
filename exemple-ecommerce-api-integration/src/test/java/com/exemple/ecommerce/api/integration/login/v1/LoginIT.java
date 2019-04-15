package com.exemple.ecommerce.api.integration.login.v1;

import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.ACCESS_APP_TOKEN;
import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.APP_HEADER;
import static com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.APP_HEADER_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.exemple.ecommerce.api.integration.core.JsonRestTemplate;

import io.restassured.response.Response;

public class LoginIT {

    public static final String URL = "/ws/v1/login";

    @Test(dependsOnMethods = "com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.connexion")
    public void exist() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .head(URL + "/{login}", "jean.dupond@gmail.com");
        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "com.exemple.ecommerce.api.integration.account.v1.AccountNominalIT.connexion")
    public void notFound() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header("Authorization", "Bearer " + ACCESS_APP_TOKEN)

                .head(URL + "/{login}", UUID.randomUUID() + "@gmail.com");
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND.value()));

    }
}
