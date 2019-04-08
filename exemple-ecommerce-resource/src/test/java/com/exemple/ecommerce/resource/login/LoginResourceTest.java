package com.exemple.ecommerce.resource.login;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.datastax.driver.core.Session;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.core.ResourceTestConfiguration;
import com.exemple.ecommerce.resource.core.statement.LoginStatement;
import com.exemple.ecommerce.resource.login.model.Login;
import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration(classes = { ResourceTestConfiguration.class })
public class LoginResourceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private LoginResource resource;

    private Login login;

    @Autowired
    private LoginStatement loginStatement;

    @Autowired
    private Session session;

    @BeforeClass
    public void save() {

        login = new Login();
        login.setLogin("jean.dupont@gmail.com");
        login.setPassword("jean.dupont");
        login.setId(UUID.randomUUID());
        login.setEnable(true);

        JsonNode source = JsonNodeUtils.create(login);

        session.execute(loginStatement.insert(source));

    }

    @Test(dependsOnMethods = "save")
    public void get() {

        JsonNode login0 = resource.get((String) login.getLogin()).get();
        assertThat(login0, not(nullValue()));
        assertThat(login0.get(LoginStatement.LOGIN).textValue(), is(login.getLogin()));
        assertThat(login0.get(LoginStatement.ID).textValue(), is(login.getId().toString()));
        assertThat(login0.get("password").textValue(), is(login.getPassword()));
        assertThat(login0.get("enable").booleanValue(), is(login.getEnable()));
    }

}
