package com.tecacet.account_manager.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class Account implements Serializable {

    public enum Type {
        CHECKING, SAVINGS
    }

    @Schema(description = "Account name", defaultValue = "Gru")
    private String name;
    @Schema(description = "Bank name", defaultValue = "Bank of Evil")
    private String bankName;
    @Schema(description = "Account number", defaultValue = "666")
    private String accountNumber;
    @Schema(description = "Balance", defaultValue = "1000000")
    private BigDecimal balance;
    @Schema(description = "Type of account (CHECKING/SAVINGS)", defaultValue = "CHECKING")
    private Type type;
}
