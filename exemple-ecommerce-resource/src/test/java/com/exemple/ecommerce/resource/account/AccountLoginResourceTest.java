package com.exemple.ecommerce.resource.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.UUID;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.datastax.driver.core.Session;
import com.exemple.ecommerce.resource.account.exception.AccountLoginResourceException;
import com.exemple.ecommerce.resource.account.exception.AccountLoginResourceExistException;
import com.exemple.ecommerce.resource.account.model.Account;
import com.exemple.ecommerce.resource.account.model.Login;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.core.ResourceTestConfiguration;
import com.exemple.ecommerce.resource.core.statement.AccountStatement;
import com.exemple.ecommerce.resource.core.statement.LoginStatement;
import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration(classes = { ResourceTestConfiguration.class })
public class AccountLoginResourceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private AccountLoginResource resource;

    private UUID id;

    private Account account;

    private Login login;

    @Autowired
    private LoginStatement loginStatement;

    @Autowired
    private AccountStatement accountStatement;

    @Autowired
    private Session session;

    @BeforeClass
    public void createAccount() {

        this.id = UUID.randomUUID();

        account = new Account();
        account.setEmail("jean.dupont@gmail.com");
        account.setLastname("Dupont");

        JsonNode source = JsonNodeUtils.create(account);
        JsonNodeUtils.set(source, id, AccountStatement.ID);

        session.execute(accountStatement.insert(source));

    }

    @Test
    public void save() throws AccountLoginResourceException {

        login = new Login();
        login.setEmail(account.getEmail());
        login.setPassword("jean.dupont");
        login.setLastname(account.getLastname());
        login.setEnable(true);

        JsonNode source = JsonNodeUtils.create(login);

        resource.save(id, source);

        JsonNode login0 = loginStatement.get(id.toString());
        assertThat(login0, not(nullValue()));
        assertThat(login0.get(LoginStatement.LOGIN).textValue(), is(id.toString()));
        assertThat(login0.get(LoginStatement.ID).textValue(), is(id.toString()));
        assertThat(login0.get("password").textValue(), is(login.getPassword()));
        assertThat(login0.get("enable").booleanValue(), is(login.getEnable()));

        JsonNode login1 = loginStatement.get((String) login.getEmail());
        assertThat(login1, not(nullValue()));
        assertThat(login1.get(LoginStatement.LOGIN).textValue(), is(login.getEmail()));
        assertThat(login1.get(LoginStatement.ID).textValue(), is(id.toString()));
        assertThat(login1.get("password").textValue(), is(login.getPassword()));
        assertThat(login1.get("enable").booleanValue(), is(login.getEnable()));

        JsonNode login2 = loginStatement.get((String) login.getLastname());
        assertThat(login2, not(nullValue()));
        assertThat(login2.get(LoginStatement.LOGIN).textValue(), is(login.getLastname()));
        assertThat(login2.get(LoginStatement.ID).textValue(), is(id.toString()));
        assertThat(login2.get("password").textValue(), is(login.getPassword()));
        assertThat(login2.get("enable").booleanValue(), is(login.getEnable()));
    }

    @Test(dependsOnMethods = "save")
    public void update() throws AccountLoginResourceException {

        Login model = new Login();
        model.setPassword("pierre.dupond");
        model.setEmail(login.getEmail());
        model.setLastname("Dupond");
        model.setFid("012345");
        model.setEnable(false);

        resource.update(id, JsonNodeUtils.create(model));

        JsonNode login0 = loginStatement.get(id.toString());
        assertThat(login0, not(nullValue()));
        assertThat(login0.get(LoginStatement.LOGIN).textValue(), is(id.toString()));
        assertThat(login0.get(LoginStatement.ID).textValue(), is(id.toString()));
        assertThat(login0.get("password").textValue(), is(model.getPassword()));
        assertThat(login0.get("enable").booleanValue(), is(model.getEnable()));

        JsonNode login1 = loginStatement.get((String) login.getEmail());
        assertThat(login1, not(nullValue()));
        assertThat(login1.get(LoginStatement.LOGIN).textValue(), is(login.getEmail()));
        assertThat(login1.get(LoginStatement.ID).textValue(), is(id.toString()));
        assertThat(login1.get("password").textValue(), is(model.getPassword()));
        assertThat(login1.get("enable").booleanValue(), is(model.getEnable()));

        JsonNode login2 = loginStatement.get((String) model.getLastname());
        assertThat(login2, not(nullValue()));
        assertThat(login2.get(LoginStatement.LOGIN).textValue(), is(model.getLastname()));
        assertThat(login2.get(LoginStatement.ID).textValue(), is(id.toString()));
        assertThat(login2.get("password").textValue(), is(model.getPassword()));
        assertThat(login2.get("enable").booleanValue(), is(model.getEnable()));

        JsonNode login3 = loginStatement.get((String) model.getFid());
        assertThat(login3, not(nullValue()));
        assertThat(login3.get(LoginStatement.LOGIN).textValue(), is(model.getFid()));
        assertThat(login3.get(LoginStatement.ID).textValue(), is(id.toString()));
        assertThat(login3.get("password").textValue(), is(model.getPassword()));
        assertThat(login3.get("enable").booleanValue(), is(model.getEnable()));

        JsonNode login4 = loginStatement.get((String) login.getLastname());
        assertThat(login4, is(nullValue()));

        Login model2 = new Login();
        model2.setEmail(null);
        model2.setPassword("p.dupond");
        model2.setEnable(true);

        resource.update(id, JsonNodeUtils.clone(JsonNodeUtils.create(model2), "lastname", "fid"));

        login0 = loginStatement.get(id.toString());
        assertThat(login0, not(nullValue()));
        assertThat(login0.get(LoginStatement.LOGIN).textValue(), is(id.toString()));
        assertThat(login0.get(LoginStatement.ID).textValue(), is(id.toString()));
        assertThat(login0.get("password").textValue(), is(model2.getPassword()));
        assertThat(login0.get("enable").booleanValue(), is(model2.getEnable()));

        login1 = loginStatement.get((String) login.getEmail());
        assertThat(login1, is(nullValue()));

        login2 = loginStatement.get((String) login.getLastname());
        assertThat(login2, is(nullValue()));

    }

    @DataProvider(name = "logins")
    public static Object[][] logins() {

        return new Object[][] { { "lastname" }, { "email" }, };

    }

    @Test(dataProvider = "logins")
    public void saveFailureLoginExist(String property) throws AccountLoginResourceException {

        String value = UUID.randomUUID().toString();

        JsonNode node = JsonNodeUtils.clone(JsonNodeUtils.create(new Login()));
        JsonNodeUtils.set(node, value, property);

        resource.save(UUID.randomUUID(), node);

        try {

            resource.save(UUID.randomUUID(), node);
            Assert.fail("LoginExistException must to be throwed");

        } catch (AccountLoginResourceExistException e) {
            assertThat(e.getLogin(), is(value));
        }
    }

    @Test
    public void saveAfterFailureLoginExist() throws AccountLoginResourceException {

        Login login0 = new Login();
        login0.setEmail(UUID.randomUUID());

        resource.save(UUID.randomUUID(), JsonNodeUtils.create(login0));

        Login login1 = new Login();
        login1.setEmail(UUID.randomUUID());
        login1.setLastname(login0.getEmail());

        try {

            resource.save(UUID.randomUUID(), JsonNodeUtils.create(login1));
            Assert.fail("LoginExistException must to be throwed");

        } catch (AccountLoginResourceExistException e) {

            Login login2 = new Login();
            login2.setEmail(login1.getEmail());

            resource.save(UUID.randomUUID(), JsonNodeUtils.create(login2));
        }
    }

    @Test
    public void updateFailureLoginExist() throws AccountLoginResourceException {

        Login model = new Login();
        model.setEmail(UUID.randomUUID().toString());

        resource.save(UUID.randomUUID(), JsonNodeUtils.create(model));

        try {

            resource.update(this.id, JsonNodeUtils.create(model));
            Assert.fail("LoginExistException must to be throwed");

        } catch (AccountLoginResourceExistException e) {
            assertThat(e.getLogin(), is(model.getEmail().toString()));
        }
    }

    @DataProvider(name = "failures")
    public static Object[][] failure() {

        return new Object[][] {
                // boolean failure
                { "enable", 10 },
                // field unknown
                { "nc", null }, };

    }

    @Test(dataProvider = "failures", expectedExceptions = ConstraintViolationException.class)
    public void saveFailure(String property, Object value) throws AccountLoginResourceException {

        JsonNode node = JsonNodeUtils.clone(JsonNodeUtils.create(new Login()));
        JsonNodeUtils.set(node, value, property);
        JsonNodeUtils.set(node, UUID.randomUUID() + "@gmail.com", LoginStatement.LOGIN);

        resource.save(UUID.randomUUID(), node);
    }

    @Test(expectedExceptions = ConstraintViolationException.class)
    public void saveFailure() throws AccountLoginResourceException {

        resource.save(UUID.randomUUID(), null);
    }
}
