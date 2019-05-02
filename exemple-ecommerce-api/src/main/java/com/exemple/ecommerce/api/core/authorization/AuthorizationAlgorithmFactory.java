package com.exemple.ecommerce.api.core.authorization;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.auth0.jwt.algorithms.Algorithm;

@Component
@Profile("!noSecurity")
public class AuthorizationAlgorithmFactory {

    private static final Pattern RSA_PUBLIC_KEY;

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationAlgorithmFactory.class);

    private final AuthorizationService authorizationService;

    private final KeyFactory keyFactory;

    private final Lock lock;

    private final ReadLock readLock;

    private final WriteLock writeLock;

    private Algorithm algorithm;

    private CountDownLatch latch;

    static {

        RSA_PUBLIC_KEY = Pattern.compile("-----BEGIN PUBLIC KEY-----(.*)-----END PUBLIC KEY-----", Pattern.DOTALL);
    }

    public AuthorizationAlgorithmFactory(AuthorizationService authorizationService) throws NoSuchAlgorithmException {

        this.authorizationService = authorizationService;
        this.keyFactory = KeyFactory.getInstance("RSA");

        this.lock = new ReentrantLock();

        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();

        this.latch = new CountDownLatch(1);

    }

    private Algorithm buildAlgorithm() throws AuthorizationException {

        Response response = this.authorizationService.tokenKey("resource", "secret");

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {

            throw new AuthorizationException("HTTP GET token_key failed",
                    new ClientErrorException(response.readEntity(String.class), response.getStatus()));
        }

        Map<String, String> body = response.readEntity(new GenericType<Map<String, String>>() {
        });

        Matcher publicKeyMatcher = RSA_PUBLIC_KEY.matcher(body.get("value"));

        Assert.isTrue(publicKeyMatcher.lookingAt(), "Pattern is invalid");

        final byte[] content = Base64.decodeBase64(publicKeyMatcher.group(1).getBytes(StandardCharsets.UTF_8));

        KeySpec keySpec = new X509EncodedKeySpec(content);
        PublicKey publicKey;
        try {
            publicKey = this.keyFactory.generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new AuthorizationException(e);
        }

        return Algorithm.RSA256((RSAPublicKey) publicKey, null);
    }

    public Algorithm getAlgorithm() throws AuthorizationException {

        if (this.algorithm == null) {

            if (this.lock.tryLock()) {
                try {
                    setAlgorithm(buildAlgorithm());
                    this.latch.countDown();
                } finally {
                    this.lock.unlock();
                }
            } else {

                try {
                    this.latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOG.error("get algorithm is interrupted!", e);
                }
            }
        }

        this.readLock.lock();
        try {
            return this.algorithm;
        } finally {
            this.readLock.unlock();
        }
    }

    public void setAlgorithm(Algorithm algorithm) {

        this.writeLock.lock();
        try {
            if (algorithm == null) {
                this.latch = new CountDownLatch(1);
            }
            this.algorithm = algorithm;
        } finally {
            this.writeLock.unlock();
        }
    }

    public void resetAlgorithm() {
        setAlgorithm(null);
    }

    public synchronized void setLatch(CountDownLatch latch) {
        this.latch = latch;

    }
}
