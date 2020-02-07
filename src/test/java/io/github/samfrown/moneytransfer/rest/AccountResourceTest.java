package io.github.samfrown.moneytransfer.rest;

import io.github.samfrown.moneytransfer.domain.Account;
import io.github.samfrown.moneytransfer.rest.request.AddAccountRequest;
import io.github.samfrown.moneytransfer.rest.response.AccountRm;
import io.github.samfrown.moneytransfer.service.AccountService;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.javamoney.moneta.FastMoney;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.github.samfrown.moneytransfer.rest.response.AccountRm.from;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountResourceTest extends JerseyTest {

    private static final String BASE_PATH = "accounts";
    private static final String NEW_ACCOUNT_ID = "new";
    private static final String FILLED_ACCOUNT_ID = "filled";
    private static final String UNKNOWN_ACCOUNT_ID = "unknown";

    private final Account newAccount = new Account(NEW_ACCOUNT_ID);
    private final Account filledAccount = new Account(FILLED_ACCOUNT_ID);

    private AccountService accountServiceMock;

    @Override
    protected Application configure() {
        ResourceConfig resourceConfig = new ResourceConfig();
        accountServiceMock = mock(AccountService.class);
        resourceConfig.register(new AccountResource(accountServiceMock));
        resourceConfig.register(JacksonFeature.class);
        //resourceConfig.register(JacksonJsonProvider.class);
        return resourceConfig;
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new JettyTestContainerFactory();
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        filledAccount.place(FastMoney.of(13.71, "USD"));
        when(accountServiceMock.find(NEW_ACCOUNT_ID)).thenReturn(newAccount);
        when(accountServiceMock.find(FILLED_ACCOUNT_ID)).thenReturn(filledAccount);
        when(accountServiceMock.create(NEW_ACCOUNT_ID)).thenReturn(newAccount);
        when(accountServiceMock.find(UNKNOWN_ACCOUNT_ID)).thenReturn(null);
    }

    @Test
    public void getAccounts_whenAccountsEmpty() {
        //when
        Response response = target(BASE_PATH).request()
                .get();
        //then
        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Http Content-Type should be: ", MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

        String accountsJson = response.readEntity(String.class);
        assertEquals("Response accounts is: ",  "[]", accountsJson);
    }

    @Test
    public void getAccount_whenUnknownId() {
        //when
        Response response = target(BASE_PATH + "/" + UNKNOWN_ACCOUNT_ID).request()
                .get();
        //then
        assertEquals("Http Response should be 404: ", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void getAccount_whenAccountIsFilled() {
        //when
        Response response = target(BASE_PATH + "/" + FILLED_ACCOUNT_ID).request()
                .get();

        //then
        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Http Content-Type should be: ", MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));
        AccountRm actualAccount = response.readEntity(AccountRm.class);
        assertEquals("Account is filled: ", from(filledAccount), actualAccount);
    }

    @Test
    public void createAccount() {
        //given
        AddAccountRequest request = new AddAccountRequest();
        request.setAccountId(NEW_ACCOUNT_ID);
        //when
        Response response = target(BASE_PATH).request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        //then
        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());
        AccountRm actualAccount = response.readEntity(AccountRm.class);
        assertEquals("Account is new: ", from(newAccount), actualAccount);
    }
}
