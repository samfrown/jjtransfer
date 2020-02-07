package io.github.samfrown.moneytransfer.dao;

import io.github.samfrown.moneytransfer.domain.Account;

import java.util.HashMap;
import java.util.Map;

public class AccountsDao {
    private final Map<String, Account> accounts;

    public AccountsDao() {
        this.accounts = new HashMap<>();
    }

    public Account selectAccount(String accountId) {
        return accounts.get(accountId);
    }

    public void addAccount(Account account) {
        accounts.put(account.getAccountId(), account);
    }
}
