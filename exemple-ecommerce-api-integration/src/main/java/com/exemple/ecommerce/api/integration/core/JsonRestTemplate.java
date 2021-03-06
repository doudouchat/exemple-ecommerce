package com.exemple.ecommerce.api.integration.core;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.internal.print.RequestPrinter;
import io.restassured.internal.print.ResponsePrinter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSpecification;

public final class JsonRestTemplate {

    public static final int TIMEOUT_CONNECTION = 3_000_000;

    public static final int TIMEOUT_SOCKET = 3_000_000;

    public static final String APPLICATION_URL = System.getProperty("application.host", "http://localhost") + ":"
            + System.getProperty("application.port", "8080") + "/" + System.getProperty("application.contextpath", "ExempleEcommerce");

    public static final String AUTHORIZATION_URL = System.getProperty("authorization.host", "http://localhost") + ":"
            + System.getProperty("authorization.port", "8084") + "/" + System.getProperty("authorization.contextpath", "ExempleAuthorization");

    private static final Logger LOG = LoggerFactory.getLogger(JsonRestTemplate.class);

    private static final RestAssuredConfig CONFIG;

    private static final AtomicInteger COUNTER;

    static {

        CONFIG = RestAssured.config().httpClient(HttpClientConfig.httpClientConfig().setParam("http.connection.timeout", TIMEOUT_CONNECTION)
                .setParam("http.socket.timeout", TIMEOUT_SOCKET));

        COUNTER = new AtomicInteger();
    }

    private JsonRestTemplate() {

    }

    public static RequestSpecification given() {

        return given(APPLICATION_URL, ContentType.JSON);
    }

    public static RequestSpecification given(String path, ContentType contentType) {

        return RestAssured.given().filters(new LoggingFilter(), new PathFilter(path)).contentType(contentType).config(CONFIG);
    }

    private static class PathFilter implements OrderedFilter {

        private final String path;

        public PathFilter(String path) {
            this.path = path;
        }

        @Override
        public int getOrder() {
            return HIGHEST_PRECEDENCE;
        }

        @Override
        public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            requestSpec.path(path + requestSpec.getUserDefinedPath());

            return ctx.next(requestSpec, responseSpec);
        }

    }

    private static class LoggingFilter implements Filter {

        @Override
        public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {

            int counter = COUNTER.incrementAndGet();

            try {
                String log = RequestPrinter.print(requestSpec, requestSpec.getMethod(), requestSpec.getURI(), LogDetail.ALL,
                        new PrintStream(new NullOutputStream(), true, StandardCharsets.UTF_8.name()), true);
                LOG.debug("Request {}\n{}", counter, log);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }

            OffsetDateTime start = OffsetDateTime.now();

            Response response = ctx.next(requestSpec, responseSpec);

            OffsetDateTime end = OffsetDateTime.now();

            long duration = ChronoUnit.MILLIS.between(start, end);

            try {
                String log = ResponsePrinter.print(response, response, new PrintStream(new NullOutputStream(), true, StandardCharsets.UTF_8.name()),
                        LogDetail.ALL, true);
                LOG.debug("Response {} {}ms\n{}", counter, duration, log);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }

            return response;
        }

    }

}
