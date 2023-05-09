package com.tecacet.cdc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName("server1.accounts.dbo.account.Envelope")
public class DebeziumMessage<T> {
    private Schema schema;
    private Payload<T> payload;

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

