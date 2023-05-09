package com.tecacet.cdc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DebeziumAccount {

    private String id;
    private String name;
    @JsonProperty("bank_name")
    private String bankName;
    @JsonProperty("account_number")
    private String accountNumber;
    private byte[] balance;
    private Account.Type type;
}
