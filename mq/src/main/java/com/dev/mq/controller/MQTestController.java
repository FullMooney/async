package com.dev.mq.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.mq.common.client.MQClient;
import com.dev.mq.domain.EventVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/async")
public class MQTestController {
    
    private final MQClient mqClient;

    public MQTestController(MQClient mqClient) {
	this.mqClient = mqClient;
    }

    @PostMapping("/publish-msg")
    public ResponseEntity<Void> publishMsg(@RequestBody EventVO vo) {
        
	mqClient.publish("x.domain.dev", "r.domain.001.dev", vo);

	return ResponseEntity.ok().build();
    }
    

}
