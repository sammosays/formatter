package com.formatter.service;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.core.MessageListener;
//import org.springframework.stereotype.Service;

import org.springframework.stereotype.Service;

@Service
public class RabbitMQListener {

    public void consumeMessage(String messageBody) {
        System.out.println("Consumed Message: " + messageBody);
    }
}

//@Service
//public class RabbitMQListener implements MessageListener {
//
//    public void onMessage(Message message) {
//        System.out.println("Consuming Message - " + new String(message.getBody()));
//    }
//}