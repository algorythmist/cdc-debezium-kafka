package com.tecacet.cdc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Payload<T> {
    private T before;
    private T after;
    private DebeziumMessage.Source source;
    private String op;
    private Long ts_ms;
    @JsonProperty("transaction")
    private TransactionBlock transactionBlock;

    @Data
    public static class TransactionBlock {
        private String id;
        private Long total_order;
        private Long data_collection_order;
    }
}
