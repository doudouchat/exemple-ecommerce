package com.exemple.ecommerce.api.integration.actuate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.exemple.ecommerce.api.integration.core.JsonRestTemplate;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class InfoIT {

    @Test
    public void info() throws IOException, URISyntaxException {

        Response response = JsonRestTemplate.given().accept(ContentType.HTML).get();
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        Document doc = Jsoup.parse(response.getBody().print());

        Element versionDiv = doc.getElementById("version");

        assertThat("La version doit être renseignée", versionDiv.text(), not(isEmptyOrNullString()));

        Matcher matcher = Pattern.compile(".*:([a-zA-Z0-9-.]*)").matcher(versionDiv.text().replaceAll(" ", ""));
        assertThat("L'expression régulière doit correspondre", matcher.find(), is(true));

    }

}
