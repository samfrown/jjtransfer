package io.github.samfrown.moneytransfer.service;

import io.github.samfrown.moneytransfer.dao.AccountsDao;
import io.github.samfrown.moneytransfer.domain.Account;
import io.github.samfrown.moneytransfer.domain.Transfer;
import io.github.samfrown.moneytransfer.rest.request.TransferFromAccountRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;

import java.util.UUID;

import static io.github.samfrown.moneytransfer.domain.Transfer.State.NEW;
import static java.lang.String.format;

public class AccountService {

    private static final Logger LOG = LoggerFactory.getLogger(AccountService.class);
    private final AccountsDao accountsDao;

    public AccountService(AccountsDao accountsDao) {
        this.accountsDao = accountsDao;
    }

    public Account find(String accountId) {
        return accountsDao.selectAccount(accountId);
    }

    public Account create(String accountId) {
        Account account = new Account(accountId);
        accountsDao.addAccount(account);
        LOG.info("Account created: {}", account);
        return accountsDao.selectAccount(accountId);
    }


}
