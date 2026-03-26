package com.example.analyzeservice.service;

import com.example.analyzeservice.model.TransactionAnalyzeEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboxProsessorImpl implements InboxProcessService {

    private final JdbcTemplate clickHouseJdbc;
    private final ObjectMapper mapper;

    @Value("${spring.analyze.inbox.batch-size}")
    private int batchSize;
    @Value("${spring.analyze.inbox.retry-max-attempts}")
    private int maxAttempts;


    @Scheduled(fixedDelayString = "${spring.analyze.inbox.processing-interval-ms}")
    @SchedulerLock(
            name = "inbox-processor",
            lockAtLeastFor = "4s",
            lockAtMostFor = "3m"
    )
    @Override
    public void processInbox() {
        List<Map<String, Object>> messages = clickHouseJdbc.queryForList(
                """
                        SELECT event_id, event_type, payload, attempts
                                       FROM analyze_inbox_tb FINAL
                                       WHERE status = 'NEW' AND attempts < ?
                                       ORDER BY created_at ASC
                                       LIMIT ?
                        """, maxAttempts, batchSize);
        if (messages.isEmpty()) {
            return;
        }
        log.info("Inbox: processing {} messages", messages.size());

        List<TransactionAnalyzeEvent> analyzeEvents = new ArrayList<>();
        List<String> succeedIs = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();

        for (Map<String, Object> mess : messages) {
            String eventId = (String) mess.get("event_id");
            try {
                TransactionAnalyzeEvent event = mapper.readValue(
                        (String) mess.get("payload"),
                        TransactionAnalyzeEvent.class
                );
                analyzeEvents.add(event);
                succeedIs.add(eventId);
            } catch (Exception e) {
                log.error("Failed to parse event ={}", eventId, e);
                failedIds.add(eventId);
            }

        }
        if (!analyzeEvents.isEmpty()) {
            try {
                insertAnalyzeTable(analyzeEvents);
                markDone(succeedIs);
                log.info("Inbox: {} events processed successfully",
                        succeedIs.size());
            } catch (Exception e) {
                log.error("Failed to insert analytics batch", e);
                incrementAttempts(succeedIs);
            }
        }

        if (!failedIds.isEmpty()) {
            markFailed(failedIds);
        }

    }

    private void insertAnalyzeTable(List<TransactionAnalyzeEvent> events) {
        clickHouseJdbc.batchUpdate("""
                INSERT INTO transactions_analyzer
                (transaction_id, customer_id, total_amount, currency,
                 payment_method, status, created_at, amount_rub)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                var event = events.get(i);
                ps.setString(1, event.transactionId());
                ps.setString(2, event.customerId());
                ps.setBigDecimal(3, event.totalAmount());
                ps.setString(4, event.currency());
                ps.setString(5, event.paymentMethod());
                ps.setString(6, event.transactionStatus());
                ps.setTimestamp(7, Timestamp.from(event.createAt().toInstant()));
                ps.setBigDecimal(8, event.amountRub());
            }

            @Override
            public int getBatchSize() {
                return events.size();
            }
        });

    }

    private void markDone(List<String> eventIds) {
        String ids = eventIds.stream()
                .map(id -> "'" + id + "'")
                .collect(Collectors.joining(","));

        clickHouseJdbc.execute(String.format("""
                ALTER TABLE analyze_inbox_tb 
                UPDATE status = 'DONE', processed_at = now64(3)
                WHERE event_id IN (%s)
                """, ids));
    }

    private void markFailed(List<String> eventIds) {
        String ids = eventIds.stream()
                .map(id -> "'" + id + "'")
                .collect(Collectors.joining(","));

        clickHouseJdbc.execute(String.format("""
                ALTER TABLE analyze_inbox_tb 
                UPDATE status = 'FAILED'
                WHERE event_id IN (%s)
                """, ids));
    }

    private void incrementAttempts(List<String> eventIds) {
        String ids = eventIds.stream()
                .map(id -> "'" + id + "'")
                .collect(Collectors.joining(","));

        clickHouseJdbc.execute(String.format("""
                ALTER TABLE analyze_inbox_tb 
                UPDATE attempts = attempts + 1
                WHERE event_id IN (%s)
                """, ids));
    }


}
