package io.github.samfrown.moneytransfer.rest.request;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;

@Data
public class TransferFromAccountRequest {
    @NotBlank
    private String toAccountId;
    @DecimalMin(value = "0.01")
    private Number transferAmount;
}

