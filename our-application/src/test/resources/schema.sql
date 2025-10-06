CREATE TABLE IF NOT EXISTS pending_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    total_price DECIMAL(15,2) NOT NULL,
    applied_year_month VARCHAR(7) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_pending_orders_username_month (username, applied_year_month)
);

CREATE TABLE IF NOT EXISTS processed_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pending_order_id BIGINT NOT NULL,
    third_party_order_id BIGINT NULL,
    status VARCHAR(32) NOT NULL,
    processed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_processed_orders_pending (pending_order_id)
);
