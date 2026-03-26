CREATE TABLE IF NOT EXISTS transactions_analyzer(
    transaction_id   String,
    customer_id      String,
    total_amount     Decimal128(2),
    currency         LowCardinality(String),  -- 'RUB', 'USD' — мало уникальных
    payment_method   LowCardinality(String),  -- 'CARD', 'SBP'
    status           LowCardinality(String),
    created_at       DateTime64(3),

    amount_rub       Decimal128(2),   -- сумма в рублях
    day              Date MATERIALIZED toDate(created_at),
    hour             UInt8 MATERIALIZED toHour(created_at)
)
    ENGINE = MergeTree()
    ORDER BY (user_id, created_at)       -- основной порядок для запросов
    PARTITION BY toYYYYMM(created_at)
    TTL created_at + INTERVAL 2 YEAR
    SETTINGS index_granularity = 8192;