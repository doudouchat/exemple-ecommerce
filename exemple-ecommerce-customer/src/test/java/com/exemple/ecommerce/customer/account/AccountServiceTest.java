package com.exemple.ecommerce.customer.account;

import static nl.fd.hamcrest.jackson.HasJsonField.hasJsonField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.ecommerce.customer.account.exception.AccountServiceException;
import com.exemple.ecommerce.customer.account.exception.AccountServiceNotFoundException;
import com.exemple.ecommerce.customer.account.model.Account;
import com.exemple.ecommerce.customer.core.CustomerExecutionContext;
import com.exemple.ecommerce.customer.core.CustomerTestConfiguration;
import com.exemple.ecommerce.resource.account.AccountResource;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
import com.exemple.ecommerce.resource.login.LoginResource;
import com.exemple.ecommerce.resource.login.exception.LoginResourceException;
import com.exemple.ecommerce.resource.login.exception.LoginResourceExistException;
import com.exemple.ecommerce.schema.common.exception.ValidationException;
import com.exemple.ecommerce.schema.filter.SchemaFilter;
import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration(classes = { CustomerTestConfiguration.class })
public class AccountServiceTest extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(AccountServiceTest.class);

    @Autowired
    private AccountService service;

    @Autowired
    private AccountResource resource;

    @Autowired
    private LoginResource loginResource;

    @Autowired
    private SchemaFilter schemaFilter;

    @BeforeMethod
    private void before() {

        CustomerExecutionContext.get().setApp("default");
        CustomerExecutionContext.get().setVersion("default");

        Mockito.reset(resource, loginResource, schemaFilter);

    }

    @AfterClass
    private void excutionContextDestroy() {

        CustomerExecutionContext.destroy();
    }

    @Test
    public void save() throws AccountServiceException {

        Account model = new Account();
        model.setEmail("jean.dupont@gmail.com");
        model.setLastname("Dupont");
        model.setFirstname("Jean");
        model.setOptinEmail(true);
        model.setCivility("Mr");

        Mockito.when(resource.save(Mockito.any(UUID.class), Mockito.any(JsonNode.class))).thenReturn(JsonNodeUtils.create(model));
        Mockito.when(
                schemaFilter.filter(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(JsonNode.class)))
                .thenReturn(JsonNodeUtils.create(model));

        JsonNode account = service.save(JsonNodeUtils.create(model));
        assertThat(account, is(notNullValue()));

        assertThat(account, hasJsonField("email", "jean.dupont@gmail.com"));
        assertThat(account, hasJsonField("lastname", "Dupont"));
        assertThat(account, hasJsonField("firstname", "Jean"));
        assertThat(account, hasJsonField("opt_in_email", true));
        assertThat(account, hasJsonField("civility", "Mr"));

    }

    @Test(expectedExceptions = AccountServiceException.class)
    public void saveFailure() throws AccountServiceException, LoginResourceException {

        Mockito.doThrow(new LoginResourceExistException("jean.dupont@gmail.com")).when(loginResource).save(Mockito.any(UUID.class),
                Mockito.any(JsonNode.class));

        Account model = new Account();
        model.setLastname("Dupont");
        model.setFirstname("Jean");

        service.save(JsonNodeUtils.create(model));
    }

    @Test
    public void update() throws AccountServiceException {

        Map<String, Object> model = new HashMap<>();
        model.put("email", "jean.dupont@gmail.com");
        model.put("lastname", "Dupont");

        UUID id = UUID.randomUUID();

        Mockito.when(resource.get(Mockito.eq(id))).thenReturn(Optional.of(JsonNodeUtils.create(model)));
        Mockito.when(resource.update(Mockito.eq(id), Mockito.any(JsonNode.class))).thenReturn(JsonNodeUtils.create(model));
        Mockito.when(
                schemaFilter.filter(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(JsonNode.class)))
                .thenReturn(JsonNodeUtils.create(model));

        JsonNode account = service.save(id, JsonNodeUtils.create(model));

        // Mockito.verify(resource).get(Mockito.eq(id));
        // Mockito.verify(resource).update(Mockito.eq(id), Mockito.any(JsonNode.class));

        assertThat(account, is(notNullValue()));

        assertThat(account, hasJsonField("email", "jean.dupont@gmail.com"));
        assertThat(account, hasJsonField("lastname", "Dupont"));

    }

    @Test(expectedExceptions = AccountServiceException.class)
    public void updateFailure() throws AccountServiceException, LoginResourceException {

        Mockito.when(resource.get(Mockito.any(UUID.class))).thenReturn(Optional.of(JsonNodeUtils.create(new Account())));
        Mockito.doThrow(new LoginResourceExistException("jean.dupont@gmail.com")).when(loginResource).update(Mockito.any(UUID.class),
                Mockito.any(JsonNode.class));

        Map<String, Object> model = new HashMap<>();

        try {
            service.save(UUID.randomUUID(), JsonNodeUtils.create(model));
        } catch (ValidationException e) {

            e.getAllExceptions().stream().map(exeception -> exeception.getMessage()).forEach(m -> LOG.debug(m));
        }
    }

    @Test
    public void get() throws AccountServiceException {

        Map<String, Object> model = new HashMap<>();
        model.put("email", "jean.dupont@gmail.com");
        model.put("lastname", "Dupont");
        model.put("firstname", "Jean");

        UUID id = UUID.randomUUID();

        Mockito.when(
                schemaFilter.filter(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(JsonNode.class)))
                .thenReturn(JsonNodeUtils.create(model));
        Mockito.when(resource.get(Mockito.eq(id))).thenReturn(Optional.of(JsonNodeUtils.create(model)));

        JsonNode account = service.get(id);
        assertThat(account, is(notNullValue()));

        assertThat(account, hasJsonField("email", "jean.dupont@gmail.com"));
        assertThat(account, hasJsonField("lastname", "Dupont"));
        assertThat(account, hasJsonField("firstname", "Jean"));

    }

    @Test(expectedExceptions = AccountServiceNotFoundException.class)
    public void getNotFound() throws AccountServiceException {

        Account model = new Account();
        model.setEmail("jean.dupont@gmail.com");
        model.setLastname("Dupont");

        UUID id = UUID.randomUUID();

        Mockito.when(resource.get(Mockito.eq(id))).thenReturn(Optional.empty());

        service.get(id);

    }

}
