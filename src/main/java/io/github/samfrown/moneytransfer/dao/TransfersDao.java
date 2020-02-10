package io.github.samfrown.moneytransfer.dao;

import io.github.samfrown.moneytransfer.domain.Transfer;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TransfersDao {

    private final Map<UUID, Transfer> transfers;

    public TransfersDao() {
        transfers = new ConcurrentHashMap<>();
    }

    public Transfer selectTransfer(UUID transferId) {
        return transfers.get(transferId);
    }

    public void addTransfer(Transfer transfer) {
        if(transfers.containsKey(transfer.getTransferId())) {
            throw new ConcurrentModificationException("Transfer with TID=" + transfer.getTransferId().toString() + " already exists");
        }
        transfers.put(transfer.getTransferId(), transfer);
    }

    public void updateTransfer(Transfer transfer) {
        if(!transfers.containsKey(transfer.getTransferId())) {
            throw new NoSuchElementException("Transfer with TID=" + transfer.getTransferId().toString() + " doesn't exist");
        }
        transfers.put(transfer.getTransferId(), transfer);
    }
}
