package io.github.samfrown.moneytransfer.rest;

import io.github.samfrown.moneytransfer.domain.Account;
import io.github.samfrown.moneytransfer.domain.Transfer;
import io.github.samfrown.moneytransfer.rest.request.AddAccountRequest;
import io.github.samfrown.moneytransfer.rest.request.DepositRequest;
import io.github.samfrown.moneytransfer.rest.request.TransferFromAccountRequest;
import io.github.samfrown.moneytransfer.rest.response.AccountRm;
import io.github.samfrown.moneytransfer.rest.response.TransferRm;
import io.github.samfrown.moneytransfer.service.AccountService;
import io.github.samfrown.moneytransfer.service.TransferService;
import org.javamoney.moneta.FastMoney;

import javax.inject.Singleton;
import javax.money.MonetaryAmount;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.UUID;

import static io.github.samfrown.moneytransfer.domain.Account.DEFAULT_CURRENCY;
import static java.lang.String.format;

@Path("accounts")
@Singleton
public class AccountResource {
    private final AccountService accountService;
    private final TransferService transferService;

    public AccountResource(AccountService accountService, TransferService transferService) {
        this.accountService = accountService;
        this.transferService = transferService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getAccounts() {
        return "[]";
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AccountRm createAccount(@Valid AddAccountRequest accountRequest) {
        Account account = accountService.create(accountRequest.getAccountId());
        return AccountRm.from(account);
    }

    @GET
    @Path("{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountRm getAccount(@PathParam("accountId") String accountId) {
        Account account = findAccount(accountId);
        return AccountRm.from(account);
    }

    @POST
    @Path("{accountId}/transfers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TransferRm createTransferFromAccount(@PathParam("accountId") String accountId,
                                                @Valid TransferFromAccountRequest transferRequest) {
        Account account = findAccount(accountId);
        Transfer transfer = transferService.createTransfer(accountId, transferRequest);
        account.getPreparedTransfers().put(transfer.getTransferId(), transfer);
        return TransferRm.from(transfer);
    }

    @PUT
    @Path("{accountId}/transfers/{transferId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TransferRm updateTransferFromAccount(@PathParam("accountId") String accountId,
                                                @PathParam("transferId") String transferId,
                                                @Valid TransferFromAccountRequest transferRequest) {
        Account account = findAccount(accountId);
        UUID transferUuid = UUID.fromString(transferId);
        if (!account.getPreparedTransfers().containsKey(transferUuid)) {
            throw new NotFoundException(format("Transfer '%s' from account '%s' is not found", accountId));
        }
        Transfer transfer = transferService.prepareTransfer(transferUuid, accountId, transferRequest);
        account.getPreparedTransfers().put(transfer.getTransferId(), transfer);
        return TransferRm.from(transfer);
    }

    @POST
    @Path("{accountId}/deposit")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public AccountRm deposit(@PathParam("accountId") String accountId,
                             @Valid DepositRequest depositRequest) {
        Account account = findAccount(accountId);
        MonetaryAmount amount = FastMoney.of(depositRequest.getAmount(), DEFAULT_CURRENCY);
        account.place(amount);
        return AccountRm.from(account);
    }

    private Account findAccount(String accountId) {
        Account account = accountService.find(accountId);
        if (account == null) {
            throw new NotFoundException(format("Account '%s' is not found", accountId));
        }
        return account;
    }
}
