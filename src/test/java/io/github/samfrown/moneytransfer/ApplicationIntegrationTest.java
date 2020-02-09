package io.github.samfrown.moneytransfer;

import io.github.samfrown.moneytransfer.domain.Transfer;
import io.github.samfrown.moneytransfer.rest.request.AddAccountRequest;
import io.github.samfrown.moneytransfer.rest.request.DepositRequest;
import io.github.samfrown.moneytransfer.rest.request.TransferFromAccountRequest;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.equalTo;

public class ApplicationIntegrationTest {
    private static final int TEST_HTTP_PORT = 19000;
    private static final String TEST_SOURCE_ACCOUNT_ID = "sourceUser";
    private static final String TEST_DEST_ACCOUNT_ID = "destUser";
    private static final String ACCOUNTS_URL = "/accounts";
    private static final String TRANSFERS_URL = "/transfers";

    private static Server jettyServer;

    private AddAccountRequest addSourceAccount;
    private AddAccountRequest addDestAccount;
    private TransferFromAccountRequest transferRequest;

    @BeforeClass
    public static void startServer() throws Exception {
        jettyServer = new Server(TEST_HTTP_PORT);
        TransferApplication app = new TransferApplication(10);
        jettyServer.setHandler(app);
        jettyServer.start();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = TEST_HTTP_PORT;
        RestAssured.config = RestAssuredConfig.config().logConfig(
                logConfig().enableLoggingOfRequestAndResponseIfValidationFails());
    }

    @Before
    public void setup() {
        addSourceAccount = new AddAccountRequest(TEST_SOURCE_ACCOUNT_ID);
        addDestAccount = new AddAccountRequest(TEST_DEST_ACCOUNT_ID);
        transferRequest = new TransferFromAccountRequest(TEST_DEST_ACCOUNT_ID, 0.01);
    }

    @Test
    public void shouldCreateAccount() {
        given().contentType(APPLICATION_JSON)
                .body(addSourceAccount)
                .when()
                .post(ACCOUNTS_URL)
                .then()
                .contentType(APPLICATION_JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("accountId", equalTo(TEST_SOURCE_ACCOUNT_ID))
                .log();
    }

    @Test
    public void shouldDepositToAccount() {
        given().contentType(APPLICATION_JSON).body(addSourceAccount)
                .when().post(ACCOUNTS_URL)
                .then().statusCode(Response.Status.OK.getStatusCode());
        given().contentType(APPLICATION_JSON).body(new DepositRequest(12345.67))
                .when().post(ACCOUNTS_URL + "/" + TEST_SOURCE_ACCOUNT_ID + "/deposit")
                .then().contentType(APPLICATION_JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("balance", equalTo("USD 12345.67000"));
    }

    @Test
    public void shouldAllowTransferPreparationBeforeCommit() {
        given().contentType(APPLICATION_JSON).body(addSourceAccount)
                .when().post(ACCOUNTS_URL)
                .then().statusCode(Response.Status.OK.getStatusCode());
        given().contentType(APPLICATION_JSON).body(addDestAccount)
                .when().post(ACCOUNTS_URL)
                .then().statusCode(Response.Status.OK.getStatusCode());
        given().contentType(APPLICATION_JSON).body(new DepositRequest(12345.67))
                .when().post(ACCOUNTS_URL + "/" + TEST_SOURCE_ACCOUNT_ID + "/deposit")
                .then().statusCode(Response.Status.OK.getStatusCode());
        transferRequest.setTransferAmount(5.1);
        String transferId = given().contentType(APPLICATION_JSON).body(transferRequest)
                .when().post(ACCOUNTS_URL + "/" + TEST_SOURCE_ACCOUNT_ID + "/transfers")
                .then().contentType(APPLICATION_JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("sourceAccountId", equalTo(TEST_SOURCE_ACCOUNT_ID),
                        "destinationAccountId", equalTo(TEST_DEST_ACCOUNT_ID),
                        "transferAmount", equalTo("USD 5.10000"))
                .extract().jsonPath().getString("transferId");
        transferRequest.setTransferAmount(2005.17);
        given().contentType(APPLICATION_JSON).body(transferRequest)
                .when().put(ACCOUNTS_URL + "/" + TEST_SOURCE_ACCOUNT_ID + "/transfers/" + transferId)
                .then().contentType(APPLICATION_JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("sourceAccountId", equalTo(TEST_SOURCE_ACCOUNT_ID),
                        "destinationAccountId", equalTo(TEST_DEST_ACCOUNT_ID),
                        "transferAmount", equalTo("USD 2005.17000"));

    }

    @Test
    public void shouldTransferMoneyFromOneAccountToAnotherAfterCommit() {

        given().contentType(APPLICATION_JSON).body(addSourceAccount)
                .when().post(ACCOUNTS_URL)
                .then().statusCode(Response.Status.OK.getStatusCode());
        given().contentType(APPLICATION_JSON).body(addDestAccount)
                .when().post(ACCOUNTS_URL)
                .then().statusCode(Response.Status.OK.getStatusCode());
        given().contentType(APPLICATION_JSON).body(new DepositRequest(12345.67))
                .when().post(ACCOUNTS_URL + "/" + TEST_SOURCE_ACCOUNT_ID + "/deposit")
                .then().statusCode(Response.Status.OK.getStatusCode());
        given().contentType(APPLICATION_JSON).body(new DepositRequest(1005.33))
                .when().post(ACCOUNTS_URL + "/" + TEST_DEST_ACCOUNT_ID + "/deposit")
                .then().statusCode(Response.Status.OK.getStatusCode());

        transferRequest.setTransferAmount(2005.17);
        String transferId = given().contentType(APPLICATION_JSON).body(transferRequest)
                .when().post(ACCOUNTS_URL + "/" + TEST_SOURCE_ACCOUNT_ID + "/transfers")
                .then().contentType(APPLICATION_JSON).statusCode(Response.Status.OK.getStatusCode())
                .body("state", equalTo(Transfer.State.NEW.name()),
                        "transferAmount", equalTo("USD 2005.17000"))
                .extract().jsonPath().getString("transferId");
        given().contentType(APPLICATION_JSON)
                .when().post(ACCOUNTS_URL + "/" + TEST_SOURCE_ACCOUNT_ID + "/transfers/" + transferId)
                .then().contentType(APPLICATION_JSON).statusCode(Response.Status.OK.getStatusCode())
                .body("state", equalTo(Transfer.State.PROCESSING.name()),
                        "transferAmount", equalTo("USD 2005.17000"));

        given().contentType(APPLICATION_JSON)
                .when().get(ACCOUNTS_URL + "/" + TEST_SOURCE_ACCOUNT_ID)
                .then().contentType(APPLICATION_JSON).statusCode(Response.Status.OK.getStatusCode())
                .body("balance", equalTo("USD 10340.50000"));
        given().contentType(APPLICATION_JSON)
                .when().get(ACCOUNTS_URL + "/" + TEST_DEST_ACCOUNT_ID)
                .then().contentType(APPLICATION_JSON).statusCode(Response.Status.OK.getStatusCode())
                .body("balance", equalTo("USD 3010.50000"));
        given().contentType(APPLICATION_JSON)
                .when().get(TRANSFERS_URL + "/" + transferId)
                .then().contentType(APPLICATION_JSON).statusCode(Response.Status.OK.getStatusCode())
                .body("state", equalTo(Transfer.State.COMPLETED.name()));
    }

    @AfterClass
    public static void stopServer() throws Exception {
        jettyServer.stop();
    }
}