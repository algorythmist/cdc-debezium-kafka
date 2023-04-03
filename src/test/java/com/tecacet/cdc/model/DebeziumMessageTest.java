package com.tecacet.cdc.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DebeziumMessageTest {

    @Test
    void decode() throws IOException {
        String filename = "debezium-message.json";
        var inputStream = DebeziumMessage.class.getClassLoader().getResourceAsStream(filename);
        ObjectMapper objectMapper = new ObjectMapper();
        var message = objectMapper.readValue(inputStream, DebeziumMessage.class);
        var schema = message.getSchema();
        assertEquals("struct", schema.getType());
        var payload = message.getPayload();
        assertEquals("c", payload.getOp());
        assertNull(payload.getBefore());
    }
}