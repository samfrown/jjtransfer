package io.github.samfrown.moneytransfer.service;

import io.github.samfrown.moneytransfer.domain.Transfer;
import io.github.samfrown.moneytransfer.dao.TransfersDao;
import io.github.samfrown.moneytransfer.rest.request.TransferFromAccountRequest;
import org.javamoney.moneta.FastMoney;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.UUID;

import static io.github.samfrown.moneytransfer.domain.Account.DEFAULT_CURRENCY;
import static io.github.samfrown.moneytransfer.domain.Transfer.State.COMPLETED;
import static io.github.samfrown.moneytransfer.domain.Transfer.State.NEW;
import static io.github.samfrown.moneytransfer.domain.Transfer.State.PROCESSING;
import static io.github.samfrown.moneytransfer.domain.Transfer.State.REJECTED;
import static io.github.samfrown.moneytransfer.domain.Transfer.State.REJECTING;

public class TransferService {
    private static final Logger LOG = LoggerFactory.getLogger(TransferService.class);

    private final TransfersDao transfersDao;
    private final Queue<UUID> transferChannel;

    public TransferService(TransfersDao transfersDao, Queue<UUID> transferChannel) {
        this.transfersDao = transfersDao;
        this.transferChannel = transferChannel;
    }

    public Transfer create(String accountId, TransferFromAccountRequest transferRequest) {
         return prepare(UUID.randomUUID(), accountId, transferRequest);
    }

    public Transfer prepare(UUID transferId, String accountId, TransferFromAccountRequest transferRequest) {
        Transfer transfer = Transfer.builder()
                .transferId(transferId)
                .sourceAccountId(accountId)
                .destinationAccountId(transferRequest.getDestinationAccountId())
                .transferAmount(FastMoney.of(transferRequest.getTransferAmount(), DEFAULT_CURRENCY))
                .state(NEW)
                .build();
        LOG.info("Transfer prepared: {}", transfer);
        return transfer;
    }

    public Transfer commit(Transfer transfer) {
        Transfer processingTransfer = transfer.toBuilder()
                .state(PROCESSING)
                .build();
        transfersDao.addTransfer(processingTransfer);
        LOG.info("Transfer saved: {}", processingTransfer);
        transferChannel.add(processingTransfer.getTransferId());
        LOG.info("Transfer '{}' committed to queue", processingTransfer.getTransferId());
        return processingTransfer;
    }

    public Transfer find(UUID transferId) {
        return transfersDao.selectTransfer(transferId);
    }

    public Transfer complete(Transfer transfer) {
        Transfer completedTransfer = transfer.toBuilder()
                .state(transfer.getState() == REJECTING ? REJECTED : COMPLETED)
                .build();
        transfersDao.updateTransfer(completedTransfer);
        LOG.info("Transfer completed: {}", completedTransfer);
        return completedTransfer;
    }

    public Transfer reject(Transfer transfer) {
        Transfer rejectingTransfer = transfer.toBuilder()
                .state(REJECTING)
                .build();
        transfersDao.updateTransfer(rejectingTransfer);
        LOG.info("Transfer rejecting: {}", transfer);
        transferChannel.add(transfer.getTransferId());
        LOG.info("Rejected transfer '{}' committed to queue", transfer.getTransferId());
        return rejectingTransfer;
    }
}
