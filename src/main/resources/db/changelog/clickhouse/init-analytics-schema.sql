-- src/main/resources/db/changelog/clickhouse/init-analytics-schema.sql

-- База данных для аналитики
CREATE DATABASE IF NOT EXISTS ttp_analytics;

-- Таблица торговых метрик
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
    ORDER BY (tn_ved_code, year, month, partner_country_code)
    SETTINGS index_granularity = 8192;

-- Таблица производственных показателей
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
    ORDER BY (okpd2_code, year, month, region)
    SETTINGS index_granularity = 8192;

-- Материализованное представление для агрегации
CREATE MATERIALIZED VIEW IF NOT EXISTS ttp_analytics.trade_metrics_yearly_mv
ENGINE = SummingMergeTree()
PARTITION BY year
ORDER BY (tn_ved_code, year, partner_country_code)
AS SELECT
              tn_ved_code,
              hs_code,
              year,
              partner_country_code,
              sum(import_value) as total_import_value,
              sum(import_quantity) as total_import_quantity,
              sum(export_value) as total_export_value,
              sum(export_quantity) as total_export_quantity
   FROM ttp_analytics.trade_metrics
   GROUP BY tn_ved_code, hs_code, year, partner_country_code;
