package com.dev.mq.Listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.dev.mq.domain.EventVO;

import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MQTestListener {

    @RabbitListener(queues = { "q.domain.001.dev" }, group = "g.domain.001.dev")
    public void listenMsg(final EventVO event) {
	log.debug("", event.toString());
	// DLQ 테스트를 위해 일부러 아무 Exception throw
	throw new ReadTimeoutException("DLQ를 테스트하자");
    }

    // DLQ를 통해 publish된 event를 받는다.
    @RabbitListener(queues = { "q.domain.001d.dev" }, group = "g.domain.001d.dev")
    public void listenDLQMsg(final EventVO event) {
	log.debug("welcome to DLQ {}", event.toString());
	// TODO DB나 스토리지에 저장하여 후처리 해보자.

    }
}
