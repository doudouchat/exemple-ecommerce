package com.exemple.ecommerce.api.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.ecommerce.api.core.JerseySpringSupport;
import com.exemple.ecommerce.api.core.feature.FeatureConfiguration;
import com.exemple.ecommerce.application.common.exception.NotFoundApplicationException;
import com.exemple.ecommerce.resource.common.util.JsonNodeUtils;
import com.exemple.ecommerce.schema.description.SchemaDescription;

public class SchemaApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private SchemaDescription service;

    @BeforeMethod
    public void before() {

        Mockito.reset(service);

    }

    public static final String URL = "/v1/schemas";

    @Test
    public void get() throws Exception {

        String resource = "account";
        String app = "default";
        String version = "v1";

        Mockito.when(service.get(Mockito.eq(app), Mockito.eq(version), Mockito.eq(resource))).thenReturn(JsonNodeUtils.init("$schema"));

        Response response = target(URL + "/" + resource + "/" + app + "/" + version).request(MediaType.APPLICATION_JSON).get();

        Mockito.verify(service).get(app, version, resource);

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
        assertThat(response.getEntity(), is(notNullValue()));

    }

    @Test
    public void getPatch() {

        String resource = "patch";

        Mockito.when(service.getPatch()).thenReturn(JsonNodeUtils.init("$schema"));

        Response response = target(URL + "/" + resource).request(MediaType.APPLICATION_JSON).get();

        Mockito.verify(service).getPatch();

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
        assertThat(response.getEntity(), is(notNullValue()));

    }

    @Test
    public void getFailureNotFoundApplicationException() throws Exception {

        String resource = "account";
        String app = "default";
        String version = "v1";

        Mockito.when(service.get(Mockito.eq(app), Mockito.eq(version), Mockito.eq(resource)))
                .thenThrow(new NotFoundApplicationException(app, new Exception()));

        Response response = target(URL + "/" + resource + "/" + app + "/" + version).request(MediaType.APPLICATION_JSON).get();

        Mockito.verify(service).get(app, version, resource);

        assertThat(response.getStatus(), is(Status.FORBIDDEN.getStatusCode()));

    }

}
