package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;
import javax.annotation.Resource;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSender {

    public static final String TOPIC_NAME = "my-topic";

    @Resource(name = "reactiveKafkaTemplate")
    private final ReactiveKafkaProducerTemplate<String, String> template;

    public Mono<Void> sendEvent(final String message) {
        return template.send(TOPIC_NAME, UUID.randomUUID().toString(), message)
            .doFirst(() -> log.info("Sending event"))
            .doOnSuccess(ignored -> log.info("Event was sent to topic {}", TOPIC_NAME))
            .doOnError(ex -> log.error("Couldn't send an event"))
            .then();
    }

}