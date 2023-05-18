package com.tecacet.cdc.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecacet.cdc.model.DebeziumAccount;
import com.tecacet.cdc.model.DebeziumAccountMessage;
import com.tecacet.cdc.model.DebeziumMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@Component
public class CdcChangeListener {
    private final ObjectMapper objectMapper = new ObjectMapper();


    @KafkaListener(topics = "${kafka.topic}", containerFactory = "kafkaListenerContainerFactory")
    public void receive(String message) throws JsonProcessingException {
        DebeziumMessage<DebeziumAccount> debeziumMessage = objectMapper.readValue(message, DebeziumAccountMessage.class);
        var payload = debeziumMessage.getPayload();
        DebeziumAccount after = payload.getAfter();
        var balance = (after == null) ? null : new BigDecimal(new BigInteger(after.getBalance()), 2);
        switch (payload.getOperationType()) {
            case CREATE:
                log.info("Received CDC create for account {} with balance {}", after.getAccountNumber(), balance);
                break;
            case UPDATE:
                log.info("Received CDC update for account {} with balance {}", after.getAccountNumber(), balance);
                break;
            case DELETE:
                log.info("Received CDC delete for account {}", payload.getBefore().getAccountNumber());
                break;
        }

    }
}
