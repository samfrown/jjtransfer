package io.github.samfrown.moneytransfer.service;

import io.github.samfrown.moneytransfer.domain.Transfer;
import io.github.samfrown.moneytransfer.dao.TransfersDao;
import io.github.samfrown.moneytransfer.rest.request.TransferFromAccountRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static io.github.samfrown.moneytransfer.domain.Transfer.State.NEW;

public class TransferService {
    private static final Logger LOG = LoggerFactory.getLogger(TransferService.class);

    private final TransfersDao transfersDao;

    public TransferService(TransfersDao transfersDao) {
        this.transfersDao = transfersDao;
    }

    public Transfer createTransfer(String accountId, TransferFromAccountRequest transferRequest) {
         return prepareTransfer(UUID.randomUUID(), accountId, transferRequest);
    }

    public Transfer prepareTransfer(UUID transferId, String accountId, TransferFromAccountRequest transferRequest) {
        Transfer transfer = Transfer.builder()
                .transferId(transferId)
                .sourceAccountId(accountId)
                .destinationAccountId(transferRequest.getToAccountId())
                .state(NEW)
                .build();
        LOG.info("Transfer prepared: {}", transfer);
        return transfer;
    }


}
