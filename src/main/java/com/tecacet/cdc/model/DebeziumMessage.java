package com.tecacet.cdc.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName("server1.accounts.dbo.account.Envelope")
public class DebeziumMessage {
    private Schema schema;
    private Payload payload;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {
        private Value before;
        private Value after;
        private Source source;
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

    @Data
    public static class Source {
        private String version;
        private String connector;
        private String name;
        private Long ts_ms;
        private String snapshot;
        private String db;
        private String sequence;
        private String schema;
        private String table;
        private String change_lsn;
        private String commit_lsn;
        private Long event_serial_no;
    }

    @Data
    public static class Value {
        private String id;
        private String name;
        @JsonProperty("bank_name")
        private String bankName;
        @JsonProperty("account_number")
        private String accountNumber;
        private byte[] balance;
        private String type;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Schema {
        private String type;
        private List<Field> fields;
        private boolean optional;
        private String name;
        private int version;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Field {
            private String type;
            private boolean optional;
            private String field;
            private String name;
            private int version;
            private Map<String, String> parameters;
        }

    }
}

