package com.exemple.ecommerce.resource.login;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.core.ResourceTestConfiguration;
import com.exemple.ecommerce.resource.core.statement.LoginStatement;
import com.exemple.ecommerce.resource.login.model.Login;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

@ContextConfiguration(classes = { ResourceTestConfiguration.class })
public class LoginResourceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private LoginResource resource;

    private Login source;

    private String login;

    @Test
    public void save() {

        source = new Login();
        source.setPassword("jean.dupont");
        source.setId(UUID.randomUUID());
        source.setEnable(true);

        login = UUID.randomUUID() + "@gmail.com";

        resource.save(login, JsonNodeUtils.create(source));

    }

    @Test(dependsOnMethods = "save")
    public void get() {

        JsonNode login0 = resource.get(login).get();
        assertThat(login0.get(LoginStatement.LOGIN).textValue(), is(login));
        assertThat(login0.get(LoginStatement.ID).textValue(), is(source.getId().toString()));
        assertThat(login0.get("password").textValue(), is(source.getPassword()));
        assertThat(login0.get("enable").booleanValue(), is(source.getEnable()));
        assertThat(login0.path("note").getNodeType(), is(JsonNodeType.MISSING));
    }

    @Test
    public void update() {

        UUID id = UUID.randomUUID();

        Login source1 = new Login();
        source1.setPassword("jean.dupont");
        source1.setId(id);
        source1.setEnable(true);

        String login1 = UUID.randomUUID() + "@gmail.com";

        resource.save(login1, JsonNodeUtils.create(source1));

        Login source2 = new Login();
        source2.setPassword("jean.dupont");
        source2.setId(id);
        source2.setEnable(true);

        String login2 = UUID.randomUUID() + "@gmail.com";

        resource.save(login2, JsonNodeUtils.create(source2));

        // update

        Map<String, Object> source = new HashMap<>();
        source.put("password", "jean.dupont2");
        source.put("enable", false);

        resource.save(login1, JsonNodeUtils.create(source));

        JsonNode data1 = resource.get(login1).get();
        assertThat(data1.get(LoginStatement.LOGIN).textValue(), is(login1));
        assertThat(data1.get(LoginStatement.ID).textValue(), is(id.toString()));
        assertThat(data1.get("password").textValue(), is(source.get("password")));
        assertThat(data1.get("enable").booleanValue(), is(source.get("enable")));

        JsonNode data2 = resource.get(login2).get();
        assertThat(data2.get(LoginStatement.LOGIN).textValue(), is(login2));
        assertThat(data2.get(LoginStatement.ID).textValue(), is(id.toString()));
        assertThat(data2.get("password").textValue(), is(source.get("password")));
        assertThat(data2.get("enable").booleanValue(), is(source.get("enable")));
    }

}
