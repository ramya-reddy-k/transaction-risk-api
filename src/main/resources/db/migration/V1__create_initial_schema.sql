CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    country VARCHAR(2) NOT NULL,
    merchant_category VARCHAR(255) NOT NULL,
    transaction_time TIMESTAMP WITH TIME ZONE NOT NULL,
    risk_score INTEGER NOT NULL,
    risk_level VARCHAR(255) NOT NULL,
    risk_reasons VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(150) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    attempts INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processing_started_at TIMESTAMP WITH TIME ZONE,
    published_at TIMESTAMP WITH TIME ZONE,
    last_error VARCHAR(1000),
    version BIGINT NOT NULL
);

CREATE INDEX idx_outbox_status_created
    ON outbox_events (status, created_at);

CREATE TABLE risk_rules (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL,
    score INTEGER NOT NULL,
    parameter_value VARCHAR(500) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_risk_rule_code UNIQUE (code)
);
