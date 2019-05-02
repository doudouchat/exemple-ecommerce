package com.exemple.ecommerce.api.core.actuate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.testng.annotations.Test;

import com.exemple.ecommerce.api.core.JerseySpringSupport;
import com.exemple.ecommerce.api.core.feature.FeatureConfiguration;

public class InfoApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    private static final String URL = "/";

    @Test
    public void info() throws Exception {

        Response response = target(URL).request().get();

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

    }

}
