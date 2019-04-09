package com.exemple.ecommerce.customer.login;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.ecommerce.customer.account.exception.AccountServiceException;
import com.exemple.ecommerce.customer.core.CustomerTestConfiguration;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.login.LoginResource;

@ContextConfiguration(classes = { CustomerTestConfiguration.class })
public class LoginServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private LoginResource resource;

    @Autowired
    private LoginService service;

    @BeforeMethod
    private void before() {

        Mockito.reset(resource);

    }

    @Test
    public void exist() {

        String login = "jean.dupond@gmail.com";

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.of(JsonNodeUtils.init()));

        service.exist(login);

        assertThat(service.exist(login), is(Boolean.TRUE));

    }

    @Test
    public void update() throws AccountServiceException {

        Map<String, Object> model = new HashMap<>();
        model.put("password", "jean.dupont");

        String login = "jean@gmail.com";

        service.save(login, JsonNodeUtils.create(model));

        // Mockito.verify(resource).save(Mockito.eq(login), Mockito.any(JsonNode.class));

    }
}
