package io.github.samfrown.moneytransfer.rest.response;

import io.github.samfrown.moneytransfer.domain.Transfer;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class TransferRm {
    @NonNull
    private final String transferId;
    @NonNull
    private final String sourceAccountId;
    @NonNull
    private final String destinationAccountId;
    @NonNull
    private final String transferAmount;
    @NonNull
    private final String state;

    public static TransferRm from(Transfer transfer) {
        return TransferRm.builder()
                .transferId(transfer.getTransferId().toString())
                .sourceAccountId(transfer.getSourceAccountId())
                .destinationAccountId(transfer.getDestinationAccountId())
                .transferAmount(transfer.getTransferAmount().toString())
                .state(transfer.getState().name())
                .build();
    }
}
