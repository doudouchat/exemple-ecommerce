package com.exemple.ecommerce.authorization.common;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import org.glassfish.jersey.message.internal.NullOutputStream;
import org.slf4j.Logger;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.filter.log.LogDetail;
import io.restassured.internal.print.RequestPrinter;
import io.restassured.internal.print.ResponsePrinter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class LoggingFilter implements Filter {

    private final Logger log;

    private static final AtomicInteger COUNTER;

    static {

        COUNTER = new AtomicInteger();
    }

    public LoggingFilter(Logger log) {
        this.log = log;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {

        int counter = COUNTER.incrementAndGet();

        try {
            String log = RequestPrinter.print(requestSpec, requestSpec.getMethod(), requestSpec.getURI(), LogDetail.ALL,
                    new PrintStream(new NullOutputStream(), true, StandardCharsets.UTF_8.name()), true);
            this.log.debug("Request {}\n{}", counter, log);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }

        Response response = ctx.next(requestSpec, responseSpec);

        try {
            String log = ResponsePrinter.print(response, response, new PrintStream(new NullOutputStream(), true, StandardCharsets.UTF_8.name()),
                    LogDetail.ALL, true);
            this.log.debug("Response {}\n{}", counter, log);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }

        return response;
    }

}
