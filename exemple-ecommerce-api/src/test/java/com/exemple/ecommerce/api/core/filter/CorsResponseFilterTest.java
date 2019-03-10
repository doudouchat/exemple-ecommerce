package com.exemple.ecommerce.api.core.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.testng.annotations.Test;

import com.exemple.ecommerce.api.core.ApiJerseyConfiguration;
import com.exemple.ecommerce.api.core.JerseySpringSupport;
import com.exemple.ecommerce.api.core.actuate.HealthApiTest;

public class CorsResponseFilterTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new ApiJerseyConfiguration();
    }

    @Test
    public void options() {

        Response response = target(HealthApiTest.URL).request(MediaType.APPLICATION_JSON).options();

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
        assertThat(response.getHeaderString("Access-Control-Allow-Methods"), is("GET, POST, DELETE, PUT, PATCH, HEAD"));
        assertThat(response.getHeaderString("Access-Control-Allow-Headers"),
                is("X-Requested-With, Content-Type, X-Codingpedia, Authorization, app, version"));

    }

}
