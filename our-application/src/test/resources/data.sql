DELETE FROM processed_orders;
DELETE FROM pending_orders;

INSERT INTO pending_orders (username, total_price, applied_year_month)
VALUES ('tester', 10000.00, '2025-02');

INSERT INTO pending_orders (username, total_price, applied_year_month)
VALUES ('second-user', 5500.00, '2025-03');
