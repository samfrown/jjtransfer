package io.github.samfrown.moneytransfer.domain;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.money.MonetaryAmount;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Value
@Builder(builderClassName = "Builder")
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

    public enum State {
        NEW,
        PROCESSING,
        COMPLETED,
        REJECTED
    }
}
