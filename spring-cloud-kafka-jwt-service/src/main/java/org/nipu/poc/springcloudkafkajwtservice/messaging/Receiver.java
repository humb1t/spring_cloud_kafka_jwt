package org.nipu.poc.springcloudkafkajwtservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.concurrent.CountDownLatch;

public class Receiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

    private CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = "${spring.kafka.topic.messaging}")
    public void receive(MessageDto payload) {
        LOGGER.info("received payload='{}'", payload);
        latch.countDown();
    }
}
