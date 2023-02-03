package com.formatter.config;

import com.formatter.service.RabbitMQListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	public static final String QUEUE_NAME = "formatter-queue";

	@Bean
	SimpleMessageListenerContainer simpleMessageListenerContainer(ConnectionFactory connectionFactory, MessageListenerAdapter messageListenerAdapter) {
		SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
		simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
		simpleMessageListenerContainer.setQueueNames(QUEUE_NAME);
		simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);
		return simpleMessageListenerContainer;
	}

	@Bean
	MessageListenerAdapter messageListenerAdapter(RabbitMQListener rabbitMQListener) {
		return new MessageListenerAdapter(rabbitMQListener, "consumeMessage");
	}
}
