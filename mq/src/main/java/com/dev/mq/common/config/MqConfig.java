package com.dev.mq.common.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableRabbit
@Configuration
public class MqConfig {
    @Value("${mq.host}")
    private String host;
    @Value("${mq.port}")
    private int port;
    @Value("${mq.username}")
    private String username;
    @Value("${mq.password}")
    private String password;
    @Value("${mq.exchange}")
    private String exchange;
    @Value("${mq.reply.timeout}")
    private Integer replyTimeout;
    @Value("${mq.concurrent.consumers}")
    private Integer concurrentConsumers;
    @Value("${mq.max.concurrent.consumers}")
    private Integer maxConcurrentConsumers;
    @Value("${mq.max.attempts}")
    private Integer maxAttempts;

    @Bean
    public DirectExchange exchange() {
	return new DirectExchange(exchange);
    }

    @Bean
    ConnectionFactory connectionFactory() {
	CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
	connectionFactory.setHost(host);
	connectionFactory.setPort(port);
	connectionFactory.setVirtualHost("/");
	connectionFactory.setUsername(username);
	connectionFactory.setPassword(password);
	return connectionFactory;
    }

    @Bean
    MessageConverter jsonMessageConverter() {
	ObjectMapper objectMapper = new ObjectMapper();
	return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean("mqTemplate")
    RabbitTemplate mqTemplate(ConnectionFactory connectionFactory) {
	final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
	rabbitTemplate.setMessageConverter(jsonMessageConverter());
	rabbitTemplate.setReplyTimeout(replyTimeout);
	rabbitTemplate.setUseDirectReplyToContainer(false);
	return rabbitTemplate;
    }

    @Bean
    AmqpAdmin amqpAdmin() {
	return new RabbitAdmin(connectionFactory());
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
	final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
	factory.setConnectionFactory(connectionFactory());
	factory.setMessageConverter(jsonMessageConverter());
	factory.setConcurrentConsumers(concurrentConsumers);
	factory.setMaxConcurrentConsumers(maxConcurrentConsumers);

	MessageRecoverer messageRecoverer = new RejectAndDontRequeueRecoverer();
	factory.setAdviceChain(
		RetryInterceptorBuilder.stateless().maxAttempts(maxAttempts).backOffOptions(3000, 2, 10000)
			.recoverer(messageRecoverer).build());
	factory.setErrorHandler(errorHandler());
	return factory;
    }

    @Bean
    ErrorHandler errorHandler() {
	return new ConditionalRejectingErrorHandler(new MyFatalExceptionStrategy());
    }

    public static class MyFatalExceptionStrategy extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {
	@Override
	public boolean isFatal(Throwable t) {
	    if (t instanceof ListenerExecutionFailedException) {
		ListenerExecutionFailedException lefe = (ListenerExecutionFailedException) t;
		if (log.isErrorEnabled()) {

		    log.error("Failed to process inbound message from queue {}: failed message: {}, {}",
			    lefe.getFailedMessage().getMessageProperties().getConsumerQueue(), lefe.getFailedMessage(),
			    t);
		}
	    }
	    return super.isFatal(t);
	}
    }
}