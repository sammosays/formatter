package com.formatter.config;

import com.formatter.service.RabbitMQListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
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


//@Configuration
//public class RabbitMQConfig {
//
//    @Value("${input.notification.rabbitmq.queue}")
//    String queueName;
//
//    @Value("${spring.rabbitmq.username}")
//    String username;
//
//    @Value("${spring.rabbitmq.password}")
//	String password;
//
//    @Value("${spring.rabbitmq.host}")
//	String host;
//
//    @Bean
//    Queue queue() {
//        return new Queue(queueName, false);
//    }
//
//    // create MessageListenerContainer using default connection factory
////    @Bean
////    MessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory) {
////        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
////        simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
////        simpleMessageListenerContainer.setQueues(queue());
////        simpleMessageListenerContainer.setMessageListener(new RabbitMQListener());
////        return simpleMessageListenerContainer;
////
////    }
//
//    // create custom connection factory
//	@Bean
//	ConnectionFactory connectionFactory() {
//		CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(host);
////		cachingConnectionFactory.setUsername(username);
////		cachingConnectionFactory.setUsername(password);
//		return cachingConnectionFactory;
//	}
//
//    // create MessageListenerContainer using custom connection factory
//	@Bean
//	MessageListenerContainer messageListenerContainer() {
//		SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
//		simpleMessageListenerContainer.setConnectionFactory(connectionFactory());
//		simpleMessageListenerContainer.setQueues(queue());
//		System.out.println("setting queue to " + queueName);
//		simpleMessageListenerContainer.setMessageListener(new RabbitMQListener());
//		return simpleMessageListenerContainer;
//	}
//}
