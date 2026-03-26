package com.example.analyzeservice.config.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootsrapServers;
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootsrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        // При первом подключении или если offset потерян — читаем с начала
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);

        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300_000); // 5 мин

        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3_000);
        // Session timeout — если нет heartbeat за это время → rebalance
        // Правило: session.timeout >= 3 * heartbeat.interval
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30_000);

        // === Fetch tuning ===
        // Минимум данных, которые broker ждёт перед ответом
        // Увеличиваем для большего батчинга (меньше сетевых вызовов)
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024); // 1KB
        // Максимальное время ожидания fetch.min.bytes
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);

        // === Isolation level ===
        // read_committed — читаем только committed сообщения
        // Важно если producer использует транзакции
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        // === Partition assignment ===
        // CooperativeStickyAssignor — при rebalance не отзывает ВСЕ партиции,
        // а только те, которые нужно перераспределить
        // Результат: меньше пауз при добавлении/удалении consumer'ов
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
                List.of(CooperativeStickyAssignor.class));

        return new DefaultKafkaConsumerFactory<>(props);

    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumerFactory);

        // === Concurrency ===
        // Количество потоков-consumer'ов
        // Каждый поток — отдельный Kafka consumer в одной consumer group
        // Если в топике 6 партиций и concurrency=3 → каждый поток читает 2 партиции
        // Нет смысла ставить concurrency > количества партиций
        factory.setConcurrency(3);

        // === Ack Mode ===
        // MANUAL — мы сами вызываем ack.acknowledge()
        // Offset коммитится только когда мы явно скажем
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.MANUAL);

        // === Commit interval ===
        // Как часто коммитить offset'ы при BATCH ack mode
        // При MANUAL — не влияет напрямую, но sync commit
        // происходит при каждом acknowledge()
        factory.getContainerProperties().setSyncCommits(true);

        // === Error Handler ===
        // DefaultErrorHandler с backoff:
        // при ошибке — retry с увеличивающимися интервалами
        // после N попыток — отправить в DLT (Dead Letter Topic)
        factory.setCommonErrorHandler(buildErrorHandler());

        // === Batch listener ===
        // false — получаем по одному сообщению в @KafkaListener
        // true — получаем List<ConsumerRecord> (для высокого throughput)
        // Для inbox паттерна одиночная обработка надёжнее
        factory.setBatchListener(false);

        return factory;
    }

    private DefaultErrorHandler buildErrorHandler() {
        var backOff = new ExponentialBackOff(1000L, 3.0);
        backOff.setMaxInterval(10_000L); // 10s;
        backOff.setMaxElapsedTime(30_000);//  not more than 30s;

        var recover = new DeadLetterPublishingRecoverer(
                kafkaTemplate(),
                (record, ex) -> new TopicPartition(
                        record.topic() + ".DLT",
                        record.partition()
                )
        );

        var errorHandler = new DefaultErrorHandler(recover, backOff);
        errorHandler.addNotRetryableExceptions(
                JsonProcessingException.class,
                NullPointerException.class,
                ClassCastException.class
        );

        errorHandler.addRetryableExceptions(
                DataAccessException.class,
                TimeoutException.class
        );

        return errorHandler;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootsrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        // Идемпотентный producer для DLT
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    /**
     * Фильтр: пропускаем tombstone-записи (key=X, value=null)
     * и события с неизвестным типом.
     */
    @Bean
    public RecordFilterStrategy<String, String> recordFilter() {
        return record -> {
            if (record.value() == null) {
                log.debug("Skipping tombstone: key={}", record.key());
                return true; // true = skip
            }
            return false; // false = process
        };
    }
}
