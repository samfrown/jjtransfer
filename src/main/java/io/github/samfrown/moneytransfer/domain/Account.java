package io.github.samfrown.moneytransfer.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.javamoney.moneta.FastMoney;

import javax.money.MonetaryAmount;

@ToString
@EqualsAndHashCode
public class Account {
    private final String accountId;
    private final FastMoney balance;

    public Account(@NonNull String accountId) {
        this.accountId = accountId;
        this.balance = FastMoney.of(0, "USD");
    }

    public String getAccountId() {
        return accountId;
    }

    public MonetaryAmount getBalance() {
        return balance.getFactory().create();
    }

    public boolean take(@NonNull MonetaryAmount amount) {
        if(amount.isGreaterThan(amount)) {
            balance.subtract(amount);
            return true;
        }
        return false;
    };

    public boolean place(@NonNull MonetaryAmount amount) {
        if(amount.isPositive()) {
            balance.add(amount);
            return true;
        }
        return false;
    };
}
