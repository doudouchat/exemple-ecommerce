package com.exemple.ecommerce.api.core.authorization;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.AnswersWithDelay;
import org.mockito.internal.stubbing.answers.DoesNothing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.exemple.ecommerce.api.account.AccountApiTest;
import com.exemple.ecommerce.api.common.model.SchemaBeanParam;
import com.exemple.ecommerce.api.core.JerseySpringSupport;
import com.exemple.ecommerce.api.core.feature.FeatureConfiguration;
import com.exemple.ecommerce.customer.account.AccountService;
import com.exemple.ecommerce.customer.account.exception.AccountServiceException;
import com.exemple.ecommerce.resource.common.util.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;

@ActiveProfiles(inheritProfiles = false)
public class AuthorizationAlgorithmFactoryTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private AccountService service;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private AuthorizationAlgorithmFactory authorizationAlgorithmFactory;

    @BeforeMethod
    private void before() {

        Mockito.reset(service);
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

    @Test
    public void authorizedMultiple() throws InterruptedException, AccountServiceException {

        String token = JWT.create().withClaim("client_id", "clientId1").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "account:create" }).sign(RSA256_ALGORITHM);

        Response responseMock = Mockito.mock(Response.class);
        Mockito.when(responseMock.getStatus()).thenReturn(Status.OK.getStatusCode());
        Mockito.when(responseMock.readEntity(new GenericType<Map<String, String>>() {
        })).thenReturn(TOKEN_KEY_RESPONSE);

        Mockito.when(service.save(Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1"))).thenReturn(JsonNodeUtils.init("id"));
        Mockito.when(authorizationService.tokenKey(Mockito.anyString(), Mockito.anyString())).thenReturn(responseMock);

        ExecutorService executorService = new ThreadPoolExecutor(10, 100, 1000, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> performService(token));
        }

        executorService.awaitTermination(5, TimeUnit.SECONDS);
        executorService.shutdown();

        Mockito.verify(service, Mockito.times(10)).save(Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1"));
        Mockito.verify(authorizationService).tokenKey(Mockito.anyString(), Mockito.anyString());

    }

    @Test
    public void authorizedMultipleFailure() throws InterruptedException, AccountServiceException {

        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch latchSpy = Mockito.spy(latch);

        authorizationAlgorithmFactory.setLatch(latchSpy);

        String token = JWT.create().withClaim("client_id", "clientId1").withAudience("test")
                .withArrayClaim("scope", new String[] { "account:create" }).sign(RSA256_ALGORITHM);

        Mockito.when(authorizationService.tokenKey(Mockito.anyString(), Mockito.anyString())).thenThrow(new RuntimeException());
        Mockito.doThrow(new InterruptedException()).when(latchSpy).await();
        Mockito.doAnswer(new AnswersWithDelay(1_000L, DoesNothing.doesNothing())).when(latchSpy).countDown();

        ExecutorService executorService = new ThreadPoolExecutor(10, 100, 1000, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> performService(token));
        }

        executorService.awaitTermination(3, TimeUnit.SECONDS);
        executorService.shutdown();

        Mockito.verify(service, Mockito.never()).save(Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1"));
        Mockito.verify(authorizationService, Mockito.atMost(10)).tokenKey(Mockito.anyString(), Mockito.anyString());

    }

    private Response performService(String token) {

        return target(AccountApiTest.URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                .post(Entity.json(JsonNodeUtils.init("email").toString()));
    }

}
