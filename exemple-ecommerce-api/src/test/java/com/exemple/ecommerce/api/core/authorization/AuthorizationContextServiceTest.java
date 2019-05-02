package com.exemple.ecommerce.api.core.authorization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.codec.binary.Base64;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.exemple.ecommerce.api.common.model.ApplicationBeanParam;
import com.exemple.ecommerce.api.common.security.ApiSecurityContext;
import com.exemple.ecommerce.api.core.ApiTestConfiguration;

@ContextConfiguration(classes = { ApiTestConfiguration.class })
public class AuthorizationContextServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private AuthorizationContextService service;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private AuthorizationAlgorithmFactory authorizationAlgorithmFactory;

    @BeforeMethod
    private void before() {

        Mockito.reset(authorizationService);

        authorizationAlgorithmFactory.resetAlgorithm();

    }

    private static final Algorithm RSA256_ALGORITHM;

    private static final Map<String, String> TOKEN_KEY_RESPONSE = new HashMap<>();

    static {

        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }

        keyPairGenerator.initialize(1024);
        KeyPair keypair = keyPairGenerator.genKeyPair();
        PrivateKey privateKey = keypair.getPrivate();
        PublicKey publicKey = keypair.getPublic();

        RSA256_ALGORITHM = Algorithm.RSA256((RSAPublicKey) publicKey, (RSAPrivateKey) privateKey);

        TOKEN_KEY_RESPONSE.put("alg", "SHA256withRSA");
        TOKEN_KEY_RESPONSE.put("value",
                "-----BEGIN PUBLIC KEY-----\n" + new String(Base64.encodeBase64(publicKey.getEncoded())) + "\n-----END PUBLIC KEY-----");

    }

    @DataProvider(name = "authorizedFailure")
    private static Object[][] failure() {

        Map<String, String> tokenKeyFailure = new HashMap<>();
        tokenKeyFailure.put("alg", "SHA256withRSA");
        tokenKeyFailure.put("value",
                "-----BEGIN PUBLIC KEY-----\n" + new String(Base64.encodeBase64("123".getBytes())) + "\n-----END PUBLIC KEY-----");

        return new Object[][] {

                { TOKEN_KEY_RESPONSE, "test", Status.BAD_REQUEST },

                { tokenKeyFailure, "test", Status.OK },

                { TOKEN_KEY_RESPONSE, "other", Status.OK }

        };
    }

    @Test(dataProvider = "authorizedFailure", expectedExceptions = AuthorizationException.class)
    public void authorizedFailure(Map<String, String> tokenKey, String application, Status status) throws AuthorizationException {

        String token = JWT.create().withClaim("user_name", "john_doe").withAudience("test").withArrayClaim("scope", new String[] { "account:write" })
                .sign(RSA256_ALGORITHM);

        Response responseMock = Mockito.mock(Response.class);
        Mockito.when(responseMock.getStatus()).thenReturn(status.getStatusCode());
        Mockito.when(responseMock.readEntity(new GenericType<Map<String, String>>() {
        })).thenReturn(tokenKey);

        Mockito.when(authorizationService.tokenKey(Mockito.anyString(), Mockito.anyString())).thenReturn(responseMock);

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle("Authorization", "Bearer " + token);
        headers.putSingle(ApplicationBeanParam.APP_HEADER, application);

        service.buildContext(headers);

    }

    @Test
    public void authorized() throws AuthorizationException {

        String token = JWT.create().withClaim("user_name", "john_doe").withAudience("test").withArrayClaim("scope", new String[] { "account:write" })
                .sign(RSA256_ALGORITHM);

        Response responseMock = Mockito.mock(Response.class);
        Mockito.when(responseMock.getStatus()).thenReturn(Status.OK.getStatusCode());
        Mockito.when(responseMock.readEntity(new GenericType<Map<String, String>>() {
        })).thenReturn(TOKEN_KEY_RESPONSE);

        Mockito.when(authorizationService.tokenKey(Mockito.anyString(), Mockito.anyString())).thenReturn(responseMock);

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle("Authorization", "Bearer " + token);
        headers.putSingle(ApplicationBeanParam.APP_HEADER, "test");

        ApiSecurityContext securityContext = service.buildContext(headers);

        Mockito.verify(authorizationService, Mockito.atMost(1)).tokenKey(Mockito.anyString(), Mockito.anyString());

        assertThat(securityContext.getUserPrincipal().getName(), is("john_doe"));
        assertThat(securityContext.isSecure(), is(true));
        assertThat(securityContext.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

    }

}
