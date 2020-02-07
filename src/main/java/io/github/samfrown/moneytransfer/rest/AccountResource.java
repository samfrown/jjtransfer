package io.github.samfrown.moneytransfer.rest;
import io.github.samfrown.moneytransfer.domain.Account;
import io.github.samfrown.moneytransfer.rest.request.AddAccountRequest;
import io.github.samfrown.moneytransfer.rest.response.AccountRm;
import io.github.samfrown.moneytransfer.service.AccountService;
import org.javamoney.moneta.FastMoney;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import javax.validation.Valid;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static java.lang.String.format;

@Path("accounts")
public class AccountResource {

    private final AccountService accountService;

    public AccountResource(AccountService accountService) {
        this.accountService = accountService;
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
        return new AccountRm(account.getAccountId(), account.getBalance().toString());
    }

    @GET
    @Path("{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountRm getAccount(@PathParam("accountId") String accountId) {
        Account account = accountService.find(accountId);
        if(account == null) {
            throw new NotFoundException(format("Account '%s' is not found", accountId));
        }
        return new AccountRm(account.getAccountId(), account.getBalance().toString());
    }
}
