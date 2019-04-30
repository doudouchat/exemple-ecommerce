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

import com.exemple.ecommerce.customer.account.context.AccountContext;
import com.exemple.ecommerce.customer.account.exception.AccountServiceException;
import com.exemple.ecommerce.customer.account.exception.AccountServiceNotFoundException;
import com.exemple.ecommerce.customer.account.model.Account;
import com.exemple.ecommerce.customer.core.CustomerTestConfiguration;
import com.exemple.ecommerce.resource.account.AccountLoginResource;
import com.exemple.ecommerce.resource.account.AccountResource;
import com.exemple.ecommerce.resource.account.exception.AccountLoginResourceException;
import com.exemple.ecommerce.resource.account.exception.AccountLoginResourceExistException;
import com.exemple.ecommerce.resource.common.JsonNodeUtils;
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
    private AccountLoginResource accountloginResource;

    @Autowired
    private SchemaFilter schemaFilter;

    private static final String APP = "default";

    private static final String VERSION = "default";

    @BeforeMethod
    private void before() {

        Mockito.reset(resource, accountloginResource, schemaFilter);

    }

    @AfterClass
    private void afterClass() {

        AccountContext.destroy();

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

        JsonNode account = service.save(JsonNodeUtils.create(model), APP, VERSION);
        assertThat(account, is(notNullValue()));

        assertThat(account, hasJsonField("email", "jean.dupont@gmail.com"));
        assertThat(account, hasJsonField("lastname", "Dupont"));
        assertThat(account, hasJsonField("firstname", "Jean"));
        assertThat(account, hasJsonField("opt_in_email", true));
        assertThat(account, hasJsonField("civility", "Mr"));

    }

    @Test(expectedExceptions = AccountServiceException.class)
    public void saveFailure() throws AccountServiceException, AccountLoginResourceException {

        Mockito.doThrow(new AccountLoginResourceExistException("jean.dupont@gmail.com")).when(accountloginResource).save(Mockito.any(UUID.class),
                Mockito.any(JsonNode.class));

        Account model = new Account();
        model.setLastname("Dupont");
        model.setFirstname("Jean");

        service.save(JsonNodeUtils.create(model), APP, VERSION);
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

        JsonNode account = service.save(id, JsonNodeUtils.create(model), APP, VERSION);

        // Mockito.verify(resource).get(Mockito.eq(id));
        // Mockito.verify(resource).update(Mockito.eq(id), Mockito.any(JsonNode.class));

        assertThat(account, is(notNullValue()));

        assertThat(account, hasJsonField("email", "jean.dupont@gmail.com"));
        assertThat(account, hasJsonField("lastname", "Dupont"));

    }

    @Test(expectedExceptions = AccountServiceException.class)
    public void updateFailure() throws AccountServiceException, AccountLoginResourceException {

        Mockito.when(resource.get(Mockito.any(UUID.class))).thenReturn(Optional.of(JsonNodeUtils.create(new Account())));
        Mockito.doThrow(new AccountLoginResourceExistException("jean.dupont@gmail.com")).when(accountloginResource).update(Mockito.any(UUID.class),
                Mockito.any(JsonNode.class));

        Map<String, Object> model = new HashMap<>();

        try {
            service.save(UUID.randomUUID(), JsonNodeUtils.create(model), APP, VERSION);
        } catch (ValidationException e) {

            e.getAllExceptions().stream().map(exception -> exception.getMessage()).forEach(m -> LOG.debug(m));
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

        JsonNode account = service.get(id, APP, VERSION);
        assertThat(account, is(notNullValue()));

        assertThat(account, hasJsonField("email", "jean.dupont@gmail.com"));
        assertThat(account, hasJsonField("lastname", "Dupont"));
        assertThat(account, hasJsonField("firstname", "Jean"));

    }

    @Test
    public void getMultiple() {

        UUID id = UUID.randomUUID();

        Mockito.when(resource.get(Mockito.eq(id))).thenReturn(Optional.of(JsonNodeUtils.init()));

        resource.get(id);
        resource.get(id);

    }

    @Test(expectedExceptions = AccountServiceNotFoundException.class)
    public void getNotFound() throws AccountServiceException {

        Account model = new Account();
        model.setEmail("jean.dupont@gmail.com");
        model.setLastname("Dupont");

        UUID id = UUID.randomUUID();

        Mockito.when(resource.get(Mockito.eq(id))).thenReturn(Optional.empty());

        service.get(id, APP, VERSION);

    }

}
