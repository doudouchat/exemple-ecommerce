package com.exemple.ecommerce.application.detail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.exemple.ecommerce.application.common.exception.NotFoundApplicationException;
import com.exemple.ecommerce.application.common.model.ApplicationDetail;
import com.exemple.ecommerce.application.core.ApplicationTestConfiguration;

@ContextConfiguration(classes = { ApplicationTestConfiguration.class })
public class ApplicationDetailServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ApplicationDetailService service;

    @Test
    public void put() {

        ApplicationDetail detail = new ApplicationDetail();
        detail.setKeyspace("keyspace1");
        detail.setCompany("company1");
        detail.setExpiryTimePassword(100L);

        service.put("app", detail);

    }

    @Test(dependsOnMethods = "put")
    public void get() {

        ApplicationDetail detail = service.get("app");

        assertThat(detail.getKeyspace(), is("keyspace1"));
        assertThat(detail.getCompany(), is("company1"));
        assertThat(detail.getExpiryTimePassword(), is(100L));

    }

    @Test
    public void getFailureNotFoundApplication() {

        String application = UUID.randomUUID().toString();

        try {

            service.get(application);

            Assert.fail("NotFoundApplicationException must be throwed");

        } catch (NotFoundApplicationException e) {

            assertThat(e.getApplication(), is(application));
        }

    }

}
