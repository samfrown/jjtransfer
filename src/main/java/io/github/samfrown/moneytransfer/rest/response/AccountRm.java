package io.github.samfrown.moneytransfer.rest.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.samfrown.moneytransfer.domain.Account;
import lombok.Value;

@Value
public class AccountRm {
    private final String accountId;
    private final String balance;

    public AccountRm(@JsonProperty("accountId") String accountId,
                     @JsonProperty("balance") String balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public static AccountRm from(Account account) {
        return new AccountRm(account.getAccountId(), account.getBalance().toString());
    }
}
