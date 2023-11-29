package com.tecacet.account_receiver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Payload<T> {

    public enum OperationType {
        CREATE, UPDATE, DELETE
    }

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

    public OperationType getOperationType() {
        switch (op) {
            case "c":
                return OperationType.CREATE;
            case "u":
                return OperationType.UPDATE;
            case "d":
                return OperationType.DELETE;
            default:
                throw new IllegalArgumentException("Unknown operation type " + op);
        }
    }

}
