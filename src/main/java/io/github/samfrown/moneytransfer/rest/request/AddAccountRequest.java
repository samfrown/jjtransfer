package io.github.samfrown.moneytransfer.rest.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AddAccountRequest {
    @NotBlank
    String accountId;
}
