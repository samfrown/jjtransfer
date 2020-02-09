package io.github.samfrown.moneytransfer.rest;

import io.github.samfrown.moneytransfer.domain.Transfer;
import io.github.samfrown.moneytransfer.rest.response.TransferRm;
import io.github.samfrown.moneytransfer.service.TransferService;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

import static java.lang.String.format;

@Path("/transfers")
@Singleton
public class TransfersResource {

    private final TransferService transferService;

    public TransfersResource(TransferService transferService) {
        this.transferService = transferService;
    }

    @GET
    @Path("{transferId}")
    @Produces(MediaType.APPLICATION_JSON)
    public TransferRm getTransfer(@PathParam("transferId") String transferId) {
        UUID transferUuid = UUID.fromString(transferId);
        Transfer transfer = transferService.find(transferUuid);
        if (transfer == null) {
            throw new NotFoundException(format("Transfer '%s' is not processing", transferUuid));
        }
        return TransferRm.from(transfer);
    }
}
