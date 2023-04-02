package com.tecacet.cdc.model;


import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class Account implements Serializable {

    public enum Type {
        CHECKING, SAVINGS
    }

    private String name;
    private String bankName;
    private String accountNumber;
    private BigDecimal balance;
    private Type type;
}
