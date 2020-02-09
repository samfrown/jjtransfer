package io.github.samfrown.moneytransfer.domain;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.money.MonetaryAmount;
import java.util.UUID;

@Value
@Builder(builderClassName = "Builder", toBuilder = true)
public class Transfer {
    @NonNull
    private final UUID transferId;
    @NonNull
    private final String sourceAccountId;
    @NonNull
    private final String destinationAccountId;
    @NonNull
    private MonetaryAmount transferAmount;
    @NonNull
    private Transfer.State state;

    public boolean canBeProcessed() {
        return state == State.PROCESSING || state == State.REJECTING;
    }

    public enum State {
        NEW,
        PROCESSING,
        REJECTING,
        COMPLETED,
        REJECTED
    }
}
