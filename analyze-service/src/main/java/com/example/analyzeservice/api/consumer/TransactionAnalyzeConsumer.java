package com.example.analyzeservice.api.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionAnalyzeConsumer {

    private final JdbcTemplate clickHouseJdbc;

    @KafkaListener(
            topics = "${spring.kafka.input-topic.name}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeEvent(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header("event_id") String eventId,
            Acknowledgment ack
    ) {

        try {
            clickHouseJdbc.update(
                    """
                            INSERT INTO analyze_inbox_tb(event_id,payload,status, kafka_topic,
                                                         kafka_partition,kafka_offset)
                            VALUES (?,?,'NEW',?,?,?)
                            """,
                    eventId,
                    payload,
                    "COMPLETED",
                    topic,
                    partition,
                    offset
            );
            ack.acknowledge();
            log.debug("Inbox: event_id ={}, offset={}", eventId, offset);
        } catch (DuplicateKeyException e) {
            // Дубликат — уже есть в inbox, просто коммитим offset
            ack.acknowledge();
            log.info("Duplicate event ignored: {}", eventId);

        } catch (Exception e) {
            // НЕ коммитим offset — при рестарте Kafka отдаст снова
            log.error("Failed to write to inbox: event_id={}", eventId, e);
            throw e;
        }
    }

}

