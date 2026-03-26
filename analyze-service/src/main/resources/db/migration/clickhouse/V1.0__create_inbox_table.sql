CREATE TABLE IF NOT EXISTS analyze_inbox_tb (
    event_id  String,
    payload   String,
    status    String DEFAULT 'NEW',
    created_at     DateTime64(3) DEFAULT now64(3),
    attemps       UInt64 DEFAULT 0,
    kafka_topic    String,
    kafka_partition UInt32,
    kafka_offset   UInt64
)
    ENGINE = ReplacingMergeTree(created_at)
    ORDER BY event_id                    -- дедупликация по event_id
    PARTITION BY toYYYYMM(created_at)    -- партиционирование по месяцам
    TTL created_at + INTERVAL 90 DAY    -- автоочистка через 90 дней
    SETTINGS index_granularity = 8192;

-- Индекс для выборки необработанных сообщений
ALTER TABLE analyze_inbox_tb
    ADD INDEX idx_status (status) TYPE set(4) GRANULARITY 1;