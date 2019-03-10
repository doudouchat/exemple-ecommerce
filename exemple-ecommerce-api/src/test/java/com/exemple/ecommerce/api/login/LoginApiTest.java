package com.exemple.ecommerce.api.login;

import static com.exemple.ecommerce.api.common.model.ApplicationBeanParam.APP_HEADER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.ecommerce.api.core.ApiJerseyConfiguration;
import com.exemple.ecommerce.api.core.JerseySpringSupport;
import com.exemple.ecommerce.customer.login.LoginService;

public class LoginApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new ApiJerseyConfiguration();
    }

    @Autowired
    private LoginService service;

    @BeforeMethod
    public void before() {

        Mockito.reset(service);

    }

    public static final String URL = "/v1/login";

    @Test
    public void check() throws Exception {

        String login = "jean.dupond@gmail.com";

        Mockito.when(service.exist(Mockito.eq(login))).thenReturn(true);

        Response response = target(URL + "/" + login).request().header(APP_HEADER, "test").head();

        assertThat(response.getStatus(), is(Status.NO_CONTENT.getStatusCode()));

        Mockito.verify(service).exist(login);

    }

    @Test
    public void checkNotFound() throws Exception {

        String login = "jean.dupond@gmail.com";

        Mockito.when(service.exist(Mockito.eq(login))).thenReturn(false);

        Response response = target(URL + "/" + login).request().header(APP_HEADER, "test").head();

        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

        Mockito.verify(service).exist(login);

    }

}
