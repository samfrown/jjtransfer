package io.github.samfrown.moneytransfer.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.javamoney.moneta.FastMoney;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
public class Account {
    public final static CurrencyUnit DEFAULT_CURRENCY = Monetary.getCurrency("USD");
    @NonNull
    private final String accountId;
    @NonNull
    private final Map<UUID, Transfer> preparedTransfers;
    @NonNull
    private final AccountBalance balance;

    public Account(@NonNull String accountId) {
        this.accountId = accountId;
        this.balance = new AccountBalance();
        this.preparedTransfers = new TreeMap<>();
    }

    public MonetaryAmount getBalance() {
        return balance.getAmount();
    }

    synchronized public boolean take(@NonNull MonetaryAmount amount) {
        if (balance.getAmount().isGreaterThanOrEqualTo(amount)) {
            balance.subtract(amount);
            return true;
        }
        return false;
    }

    synchronized public boolean place(@NonNull MonetaryAmount amount) {
        if (amount.isPositive()) {
            balance.add(amount);
            return true;
        }
        return false;
    }

    private static class AccountBalance {
        private MonetaryAmount balanceAmount;

        AccountBalance() {
            balanceAmount = FastMoney.of(0, DEFAULT_CURRENCY);
        }

        MonetaryAmount getAmount() {
            return balanceAmount;
        }

        void subtract(MonetaryAmount amount) {
            balanceAmount = balanceAmount.subtract(amount);
        }

        void add(MonetaryAmount amount) {
            balanceAmount = balanceAmount.add(amount);
        }
    }
}
