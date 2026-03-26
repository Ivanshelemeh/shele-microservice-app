CREATE TABLE IF NOT EXISTS daily_user_stats
(
    customer_id    String,
    day            Date,
    total_amount   AggregateFunction(sum, Decimal128(2)),
    tx_count       AggregateFunction(count),
    avg_amount     AggregateFunction(avg, Decimal128(2)),
    max_amount     AggregateFunction(max, Decimal128(2))
    )
    ENGINE = AggregatingMergeTree()
    ORDER BY (user_id, day)
    PARTITION BY toYYYYMM(day);


-- Materialized View: автоматически агрегирует при INSERT
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_daily_user_stats
TO daily_user_stats
AS SELECT
              customer_id,
              toDate(created_at) AS day,
    sumState(amount_rub) AS total_amount,
    countState() AS tx_count,
    avgState(amount_rub) AS avg_amount,
    maxState(amount_rub) AS max_amount
   FROM transactions_analytics
   GROUP BY user_id, day;

