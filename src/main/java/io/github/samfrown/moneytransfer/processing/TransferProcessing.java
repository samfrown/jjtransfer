package io.github.samfrown.moneytransfer.processing;

import io.github.samfrown.moneytransfer.domain.Account;
import io.github.samfrown.moneytransfer.domain.Transfer;
import io.github.samfrown.moneytransfer.service.AccountService;
import io.github.samfrown.moneytransfer.service.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class TransferProcessing implements Runnable {
    private static Logger LOG = LoggerFactory.getLogger(TransferProcessing.class);
    private final TransferService transferService;
    private final BlockingQueue<UUID> transferChannel;
    private final AccountService accountService;

    public TransferProcessing(AccountService accountService, TransferService transferService, BlockingQueue<UUID> transferChannel) {
        this.accountService = accountService;
        this.transferService = transferService;
        this.transferChannel = transferChannel;
    }

    @Override
    public void run() {
        LOG.info("Transfer Processing started");
        try {
            while (true) {
                UUID transferId = transferChannel.take();
                Transfer transfer = transferService.find(transferId);
                LOG.info("Next transfer received: {} ", transfer);
                if (!transfer.canBeProcessed()) {
                    LOG.error("Received transfer can not be processed. Skip transfer: {}", transfer);
                    continue;
                }
                processTransfer(transfer);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void processTransfer(Transfer transfer) {
        try {
            Account destination = accountService.find(transfer.getDestinationAccountId());
            destination.place(transfer.getTransferAmount());
            transferService.complete(transfer);
        } catch (Exception e) {
            LOG.error("Error processing transfer: {}", transfer, e);
            if (transfer.getState() == Transfer.State.PROCESSING) {
                LOG.info("Transfer will be returned to source account: {}", transfer.getSourceAccountId());
                transferService.reject(transfer);
            }
        }
    }
}
