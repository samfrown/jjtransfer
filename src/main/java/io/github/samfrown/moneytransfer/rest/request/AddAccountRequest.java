package io.github.samfrown.moneytransfer.rest.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddAccountRequest {
    @NotBlank
    String accountId;
}
