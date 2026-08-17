-- ============================================================
-- Performance benchmark dataset
-- Creates:
--   1 user
--   4 accounts
--   8 expense categories
--   2 income categories
--   100,000 transactions
-- ============================================================

BEGIN;

-- ------------------------------------------------------------
-- Benchmark user
-- ------------------------------------------------------------

INSERT INTO users (
    name,
    email,
    created_at,
    updated_at
)
VALUES (
           'Performance Test User',
           'performance@example.com',
           NOW(),
           NOW()
       );

-- ------------------------------------------------------------
-- Accounts
-- ------------------------------------------------------------

INSERT INTO accounts (
    user_id,
    name,
    type,
    opening_balance,
    created_at,
    updated_at
)
SELECT
    u.id,
    account.name,
    account.type,
    account.opening_balance,
    NOW(),
    NOW()
FROM users u
         CROSS JOIN (
    VALUES
        ('Benchmark Chequing', 'CHEQUING', 5000.00::NUMERIC),
        ('Benchmark Savings', 'SAVINGS', 10000.00::NUMERIC),
        ('Benchmark Credit Card', 'CREDIT_CARD', 0.00::NUMERIC),
        ('Benchmark Cash', 'CASH', 500.00::NUMERIC)
) AS account(name, type, opening_balance)
WHERE u.email = 'performance@example.com';

-- ------------------------------------------------------------
-- Categories
-- ------------------------------------------------------------

INSERT INTO categories (
    user_id,
    name,
    type,
    created_at,
    updated_at
)
SELECT
    u.id,
    category.name,
    category.type,
    NOW(),
    NOW()
FROM users u
         CROSS JOIN (
    VALUES
        ('Salary', 'INCOME'),
        ('Freelance', 'INCOME'),

        ('Groceries', 'EXPENSE'),
        ('Dining', 'EXPENSE'),
        ('Transportation', 'EXPENSE'),
        ('Utilities', 'EXPENSE'),
        ('Entertainment', 'EXPENSE'),
        ('Shopping', 'EXPENSE'),
        ('Rent', 'EXPENSE'),
        ('Miscellaneous', 'EXPENSE')
) AS category(name, type)
WHERE u.email = 'performance@example.com';


-- ------------------------------------------------------------
-- 100,000 transactions
-- ------------------------------------------------------------

WITH benchmark_user AS (
    SELECT id
    FROM users
    WHERE email = 'performance@example.com'
),

     user_accounts AS (
         SELECT
             a.id,
             a.name,
             ROW_NUMBER() OVER (ORDER BY a.id) AS rn
         FROM accounts a
                  JOIN benchmark_user u
                       ON a.user_id = u.id
     ),

     expense_categories AS (
         SELECT
             c.id,
             ROW_NUMBER() OVER (ORDER BY c.id) AS rn
         FROM categories c
                  JOIN benchmark_user u
                       ON c.user_id = u.id
         WHERE c.type = 'EXPENSE'
     ),

     income_categories AS (
         SELECT
             c.id,
             ROW_NUMBER() OVER (ORDER BY c.id) AS rn
         FROM categories c
                  JOIN benchmark_user u
                       ON c.user_id = u.id
         WHERE c.type = 'INCOME'
     ),

     generated AS (
         SELECT
             gs,
             random() AS type_random,
             random() AS account_random,
             random() AS category_random,

             DATE '2024-01-01'
                 + floor(
                     random() *
                     (DATE '2026-09-01' - DATE '2024-01-01')
                   )::int AS transaction_date,

             round(
                     (
                         5 + random() * 495
                         )::numeric,
                     2
             ) AS amount

         FROM generate_series(1, 100000) AS gs
     )

INSERT INTO transactions (
    type,
    amount,
    account_id,
    source_account_id,
    destination_account_id,
    category_id,
    description,
    transaction_date,
    created_at,
    updated_at
)

SELECT
    CASE
        WHEN g.type_random < 0.75 THEN 'EXPENSE'
        WHEN g.type_random < 0.90 THEN 'INCOME'
        ELSE 'TRANSFER'
        END,

    g.amount,

    CASE
        WHEN g.type_random < 0.90
            THEN (
            SELECT id
            FROM user_accounts
            WHERE rn =
                  floor(g.account_random * 4)::int + 1
        )
        ELSE NULL
END,

    CASE
        WHEN g.type_random >= 0.90
        THEN (
            SELECT id
            FROM user_accounts
            WHERE rn =
                floor(g.account_random * 4)::int + 1
        )
        ELSE NULL
END,

    CASE
        WHEN g.type_random >= 0.90
        THEN (
            SELECT id
            FROM user_accounts
            WHERE rn =
                (
                    floor(g.account_random * 4)::int + 1
                ) % 4 + 1
        )
        ELSE NULL
END,

    CASE
        WHEN g.type_random < 0.75
        THEN (
            SELECT id
            FROM expense_categories
            WHERE rn =
                floor(g.category_random * 8)::int + 1
        )

        WHEN g.type_random < 0.90
        THEN (
            SELECT id
            FROM income_categories
            WHERE rn =
                floor(g.category_random * 2)::int + 1
        )

        ELSE NULL
END,

    'Synthetic benchmark transaction ' || g.gs,

    g.transaction_date,

    NOW(),
    NOW()

FROM generated g;

COMMIT;