package com.tecacet.cdc;

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


    @KafkaListener(topics = "${kafka.topic}",
            containerFactory = "kafkaListenerContainerFactory")
    public void receive(String message) {
        //var payload = message.getPayload();

        try {
            DebeziumMessage<DebeziumAccount> debeziumMessage = objectMapper.readValue(message, DebeziumAccountMessage.class);
            var payload = debeziumMessage.getPayload();
            DebeziumAccount value = payload.getAfter();
            BigDecimal balance= new BigDecimal(new BigInteger(value.getBalance()), 2);
            log.info("Received CDC change for account: {} with balance ${}", value, balance);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
