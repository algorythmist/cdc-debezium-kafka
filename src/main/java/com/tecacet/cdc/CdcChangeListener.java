package com.tecacet.cdc;

import com.tecacet.cdc.model.DebeziumMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CdcChangeListener {

    @KafkaListener(topics = "${kafka.topic}",
            containerFactory = "kafkaListenerContainerFactory")
    public void receive(DebeziumMessage message) {
        var payload = message.getPayload();
        log.info("Received CDC change with payload: {}", payload);

    }
}
