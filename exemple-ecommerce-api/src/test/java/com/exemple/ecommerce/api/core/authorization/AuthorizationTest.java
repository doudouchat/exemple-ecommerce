package com.exemple.ecommerce.api.core.authorization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.exemple.ecommerce.api.common.model.SchemaBeanParam;
import com.exemple.ecommerce.api.core.ApiJerseyConfiguration;
import com.exemple.ecommerce.api.core.JerseySpringSupport;
import com.exemple.ecommerce.customer.account.AccountService;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;

@ActiveProfiles(inheritProfiles = false)
public class AuthorizationTest extends JerseySpringSupport {

    private TestFilter testFilter = new TestFilter();

    @Override
    protected ResourceConfig configure() {
        return new ApiJerseyConfiguration().register(testFilter);
    }

    @Autowired
    private AccountService service;

    @Autowired
    private AuthorizationService authorizationService;

    @BeforeMethod
    private void before() {

        Mockito.reset(service);
        Mockito.reset(authorizationService);

    }

    public static final String URL = "/v1/account";

    @DataProvider(name = "notAuthorized")
    private static Object[][] notAuthorized() {

        Algorithm algorithm = Algorithm.HMAC256("secret");
        UUID id = UUID.randomUUID();

        String token1 = JWT.create().withClaim("id", id.toString()).withArrayClaim("authorities", new String[] { "ROLE_ACCOUNT" })
                .withAudience("test").withArrayClaim("scope", new String[] { "account:unknown" }).sign(algorithm);
        String payload1 = StringUtils.newStringUtf8(Base64.decodeBase64(JWT.decode(token1).getPayload()));

        String token2 = JWT.create().withClaim("id", id.toString()).withArrayClaim("authorities", new String[] { "ROLE_ACCOUNT" })
                .withAudience("test").withArrayClaim("scope", new String[] { "account:write" }).sign(algorithm);
        String payload2 = StringUtils.newStringUtf8(Base64.decodeBase64(JWT.decode(token2).getPayload()));

        String token3 = JWT.create().withClaim("id", id.toString()).withClaim("user_name", "john_doe")
                .withArrayClaim("authorities", new String[] { "ROLE_ACCOUNT" }).withAudience("test")
                .withArrayClaim("scope", new String[] { "account:read" }).sign(algorithm);
        String payload3 = StringUtils.newStringUtf8(Base64.decodeBase64(JWT.decode(token3).getPayload()));

        String token4 = JWT.create().withClaim("id", id.toString()).withClaim("user_name", "john_doe").withClaim("id", UUID.randomUUID().toString())
                .withArrayClaim("authorities", new String[] { "ROLE_ACCOUNT" }).withAudience("test")
                .withArrayClaim("scope", new String[] { "account:read" }).sign(algorithm);
        String payload4 = StringUtils.newStringUtf8(Base64.decodeBase64(JWT.decode(token4).getPayload()));

        return new Object[][] {

                { "token", null, "test", id, Status.BAD_REQUEST },

                { token1, payload1, "test", id, Status.OK },

                { token2, payload2, "test", id, Status.OK },

                { token3, payload3, "test", UUID.randomUUID(), Status.OK },

                { token4, payload4, "other", id, Status.OK },

                { null, payload4, "test", id, Status.OK }

        };
    }

    @Test(dataProvider = "notAuthorized")
    public void notAuthorized(String token, String payload, String application, UUID id, Status status) throws Exception {

        Response responseMock = Mockito.mock(Response.class);
        Mockito.when(responseMock.getStatus()).thenReturn(status.getStatusCode());
        Mockito.when(responseMock.readEntity(String.class)).thenReturn(payload);

        Mockito.when(authorizationService.checkToken(Mockito.eq(token), Mockito.anyString(), Mockito.anyString())).thenReturn(responseMock);

        Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, application).header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        if (token != null) {
            Mockito.verify(authorizationService).checkToken(Mockito.eq(token), Mockito.anyString(), Mockito.anyString());
        } else {
            Mockito.verify(authorizationService, Mockito.never()).checkToken(Mockito.eq(token), Mockito.anyString(), Mockito.anyString());

        }

        assertThat(response.getStatus(), is(Status.FORBIDDEN.getStatusCode()));

    }

    @Test
    public void authorizedHealth() throws Exception {

        Response response = target("/health").request(MediaType.APPLICATION_JSON).get();

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

    }

    @Test
    public void authorizedUser() throws Exception {

        UUID id = UUID.randomUUID();

        Algorithm algorithm = Algorithm.HMAC256("secret");
        String token = JWT.create().withClaim("id", id.toString()).withClaim("user_name", "john_doe")
                .withArrayClaim("authorities", new String[] { "ROLE_ACCOUNT" }).withAudience("test")
                .withArrayClaim("scope", new String[] { "account:read" }).sign(algorithm);

        DecodedJWT jwt = JWT.decode(token);
        String payload = StringUtils.newStringUtf8(Base64.decodeBase64(jwt.getPayload()));

        Response responseMock = Mockito.mock(Response.class);
        Mockito.when(responseMock.getStatus()).thenReturn(Status.OK.getStatusCode());
        Mockito.when(responseMock.readEntity(String.class)).thenReturn(payload);

        Mockito.when(service.get(Mockito.eq(id))).thenReturn(JsonNodeUtils.init("email"));
        Mockito.when(authorizationService.checkToken(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(responseMock);

        Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        Mockito.verify(service).get(id);
        Mockito.verify(authorizationService).checkToken(Mockito.eq(token), Mockito.anyString(), Mockito.anyString());

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        assertThat(testFilter.context.getUserPrincipal().getName(), is(id.toString()));
        assertThat(testFilter.context.isUserInRole("ROLE_ACCOUNT"), is(true));
        assertThat(testFilter.context.isSecure(), is(true));
        assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

    }

    @Test
    public void authorizedApp() throws Exception {

        Algorithm algorithm = Algorithm.HMAC256("secret");
        String token = JWT.create().withClaim("client_id", "test").withArrayClaim("authorities", new String[] { "ROLE_APP" }).withAudience("test")
                .withArrayClaim("scope", new String[] { "account:create" }).sign(algorithm);

        DecodedJWT jwt = JWT.decode(token);
        String payload = StringUtils.newStringUtf8(Base64.decodeBase64(jwt.getPayload()));

        Response responseMock = Mockito.mock(Response.class);
        Mockito.when(responseMock.getStatus()).thenReturn(Status.OK.getStatusCode());
        Mockito.when(responseMock.readEntity(String.class)).thenReturn(payload);

        Mockito.when(service.save(Mockito.any(JsonNode.class))).thenReturn(JsonNodeUtils.init("id"));
        Mockito.when(authorizationService.checkToken(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(responseMock);

        Response response = target(URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                .post(Entity.json(JsonNodeUtils.init("email").toString()));

        Mockito.verify(service).save(Mockito.any(JsonNode.class));
        Mockito.verify(authorizationService).checkToken(Mockito.eq(token), Mockito.anyString(), Mockito.anyString());

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        assertThat(testFilter.context.getUserPrincipal().getName(), is("test"));
        assertThat(testFilter.context.isUserInRole("ROLE_APP"), is(true));
        assertThat(testFilter.context.isSecure(), is(true));
        assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

    }

    public static class TestFilter implements ContainerRequestFilter {

        SecurityContext context;

        @Override
        public void filter(ContainerRequestContext requestContext) {

            context = requestContext.getSecurityContext();

        }

    }
}
