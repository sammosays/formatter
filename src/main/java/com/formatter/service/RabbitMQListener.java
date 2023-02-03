package com.formatter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitMQListener {

    public void consumeMessage(byte[] message) {
        log.info("Consumed Message: " + message);
    }
}