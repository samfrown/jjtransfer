package io.github.samfrown.moneytransfer.rest;

import io.github.samfrown.moneytransfer.domain.Account;
import io.github.samfrown.moneytransfer.rest.request.AddAccountRequest;
import io.github.samfrown.moneytransfer.rest.request.DepositRequest;
import io.github.samfrown.moneytransfer.rest.response.AccountRm;
import io.github.samfrown.moneytransfer.service.AccountService;
import io.github.samfrown.moneytransfer.service.TransferService;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.javamoney.moneta.FastMoney;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.github.samfrown.moneytransfer.domain.Account.DEFAULT_CURRENCY;
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
    private TransferService transferServiceMock;

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig();
        accountServiceMock = mock(AccountService.class);
        transferServiceMock = mock(TransferService.class);
        resourceConfig.register(new AccountsResource(accountServiceMock, transferServiceMock));
        resourceConfig.property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_SERVER, "INFO");
        return resourceConfig;
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        filledAccount.place(FastMoney.of(13.71, DEFAULT_CURRENCY));
        when(accountServiceMock.find(NEW_ACCOUNT_ID)).thenReturn(newAccount);
        when(accountServiceMock.find(FILLED_ACCOUNT_ID)).thenReturn(filledAccount);
        when(accountServiceMock.create(NEW_ACCOUNT_ID)).thenReturn(newAccount);
        when(accountServiceMock.find(UNKNOWN_ACCOUNT_ID)).thenReturn(null);
    }

    @Test
    public void getAccount_whenUnknownId() {
        //when
        Response response = target(BASE_PATH + "/" + UNKNOWN_ACCOUNT_ID).request()
                .get();
        response.readEntity(String.class);
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

    @Test
    public void depositToNewAccount() {
        //given
        DepositRequest request = new DepositRequest();
        request.setDepositAmount(1.01);
        //when
        Response response = target(BASE_PATH + "/" + NEW_ACCOUNT_ID + "/deposit")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        //then
        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());

        AccountRm actualAccount = response.readEntity(AccountRm.class);
        assertEquals("Account is new: ", NEW_ACCOUNT_ID, actualAccount.getAccountId());
        assertEquals("Balance is: ", "USD 1.01000", actualAccount.getBalance());
    }

}
