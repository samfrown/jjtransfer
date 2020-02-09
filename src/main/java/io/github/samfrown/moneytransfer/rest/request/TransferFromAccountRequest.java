package io.github.samfrown.moneytransfer.rest.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferFromAccountRequest {
    @NotBlank
    private String toAccountId;
    @DecimalMin(value = "0.01")
    private Number transferAmount;
}

