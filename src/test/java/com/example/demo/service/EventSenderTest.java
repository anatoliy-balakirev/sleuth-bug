package com.example.demo.service;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("component-test")
@EmbeddedKafka
@DirtiesContext
class EventSenderTest {
    private static final String MESSAGE = "Some message";
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private EventSender eventSender;

    private KafkaConsumer<String, String> kafkaConsumer;

    @BeforeEach
    void setUp() {
        kafkaConsumer = createKafkaConsumer();
    }

    @AfterEach
    void cleanUp() {
        kafkaConsumer.close();
    }

    @Test
    void testSendEvent() {
        StepVerifier.create(eventSender.sendEvent(MESSAGE))
            .expectNextCount(0)
            .verifyComplete();

        final ConsumerRecord<String, String> message =
            KafkaTestUtils.getSingleRecord(kafkaConsumer, EventSender.TOPIC_NAME);

        assertNotNull(message);
        assertEquals(MESSAGE, message.value());
    }

    private KafkaConsumer<String, String> createKafkaConsumer() {
        final Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testConsumer", "false",
            embeddedKafkaBroker);
        // Offset is set as `earliest`, because by the time consumer starts, producer might have sent all messages:
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        final KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(consumerProps);
        kafkaConsumer.subscribe(singletonList(EventSender.TOPIC_NAME));
        return kafkaConsumer;
    }
}
