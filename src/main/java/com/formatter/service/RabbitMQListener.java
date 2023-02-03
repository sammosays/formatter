package com.formatter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQListener {

    Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);

    public void consumeMessage(String messageBody) {
        logger.info("Consumed Message: " + messageBody);
    }
}