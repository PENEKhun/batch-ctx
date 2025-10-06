INSERT INTO pending_orders (username, total_price, applied_year_month)
SELECT 'tester', 10000.00, '2025-02'
WHERE NOT EXISTS (
    SELECT 1 FROM pending_orders WHERE username = 'tester' AND applied_year_month = '2025-02'
);

INSERT INTO pending_orders (username, total_price, applied_year_month)
SELECT 'second-user', 5500.00, '2025-03'
WHERE NOT EXISTS (
    SELECT 1 FROM pending_orders WHERE username = 'second-user' AND applied_year_month = '2025-03'
);
