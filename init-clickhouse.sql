CREATE DATABASE IF NOT EXISTS ttp_analytics;

CREATE TABLE IF NOT EXISTS ttp_analytics.trade_metrics
(
    tn_ved_code String,
    hs_code String,
    year UInt16,
    month UInt8,
    import_value Float64,
    import_quantity Float64,
    export_value Float64,
    export_quantity Float64,
    partner_country String,
    partner_country_code String,
    created_at DateTime DEFAULT now()
    )
    ENGINE = MergeTree()
    PARTITION BY toYYYYMM(toDate(concat(toString(year), '-', toString(month), '-01')))
    ORDER BY (tn_ved_code, year, month, partner_country_code);

CREATE TABLE IF NOT EXISTS ttp_analytics.production_stats
(
    okpd2_code String,
    year UInt16,
    month UInt8,
    production_volume Float64,
    production_capacity Float64,
    capacity_utilization Float32,
    region String,
    created_at DateTime DEFAULT now()
    )
    ENGINE = MergeTree()
    PARTITION BY toYYYYMM(toDate(concat(toString(year), '-', toString(month), '-01')))
    ORDER BY (okpd2_code, year, month, region);

CREATE TABLE IF NOT EXISTS ttp_analytics.classification_logs
(
    request_id String,
    user_id String,
    product_name String,
    tn_ved_code String,
    confidence Float32,
    processing_time_ms UInt32,
    created_at DateTime DEFAULT now()
    )
    ENGINE = MergeTree()
    PARTITION BY toYYYYMM(created_at)
    ORDER BY (created_at, request_id);
