package org.nipu.poc.springcloudkafkajwtservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;

import java.util.concurrent.CountDownLatch;

public class Receiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

    private CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = "${spring.kafka.topic.messaging}")
    public void receive(MessageContainer payload) {
        LOGGER.info("received payload='{}'", payload);
        final Jwt jwt = JwtHelper.decode((String) payload.getMessage());
        LOGGER.info("Jwt decoded: {}", jwt);
        latch.countDown();
    }
}
