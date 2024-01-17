package com.dev.mq.common.client;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MQClient {

    private final RabbitTemplate mqTemplate;

    public MQClient(@Qualifier("mqTemplate") RabbitTemplate mqTemplate) {
	this.mqTemplate = mqTemplate;
    }

    public <T> void publish(final String exchange, final String routingKey, T event) {

	try {

	    if (StringUtils.isEmpty(exchange)) {
		throw new NoSuchMethodException("[ERROR] exchange is mandatory. ");
	    }

	    if (StringUtils.isEmpty(routingKey)) {
		throw new NoSuchMethodException("[ERROR] routing-key is mandatory. ");
	    }

	    mqTemplate.convertAndSend(exchange, routingKey, event);


	} catch (AmqpException | NoSuchMethodException | SecurityException | IllegalArgumentException e) {
	    throw new RuntimeException(e.getMessage());
	}

    }
}
