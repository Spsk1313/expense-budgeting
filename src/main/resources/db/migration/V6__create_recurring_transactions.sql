CREATE TABLE recurring_transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id BIGINT NOT NULL REFERENCES users(id),

    type VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,

    account_id BIGINT REFERENCES accounts(id),
    source_account_id BIGINT REFERENCES accounts(id),
    destination_account_id BIGINT REFERENCES accounts(id),
    category_id BIGINT REFERENCES categories(id),

    description TEXT,

    frequency VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    next_run_date DATE NOT NULL,
    preferred_day_of_month INTEGER,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_recurring_transaction_type
        CHECK (type IN ('INCOME', 'EXPENSE', 'TRANSFER')),

    CONSTRAINT chk_recurring_transaction_amount
        CHECK (amount > 0),

    CONSTRAINT chk_recurring_transaction_shape
        CHECK (
            (
                type IN ('INCOME', 'EXPENSE')
                    AND account_id IS NOT NULL
                    AND category_id IS NOT NULL
                    AND source_account_id IS NULL
                    AND destination_account_id IS NULL
                )
                OR
            (
                type = 'TRANSFER'
                    AND account_id IS NULL
                    AND category_id IS NULL
                    AND source_account_id IS NOT NULL
                    AND destination_account_id IS NOT NULL
                    AND source_account_id <> destination_account_id
                )
            ),

    CONSTRAINT chk_recurring_transaction_frequency
        CHECK (frequency IN ('WEEKLY', 'MONTHLY')),

    CONSTRAINT chk_recurring_transaction_schedule
        CHECK (
            (
                frequency = 'WEEKLY'
                    AND preferred_day_of_month IS NULL
                )
                OR
            (
                frequency = 'MONTHLY'
                    AND preferred_day_of_month IS NOT NULL
                    AND preferred_day_of_month BETWEEN 1 AND 31
                )
            ),

    CONSTRAINT chk_recurring_transaction_next_run
        CHECK (next_run_date >= start_date)
);

CREATE TABLE recurring_transaction_occurrences (
   id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

   recurring_transaction_id BIGINT NOT NULL
       REFERENCES recurring_transactions(id),

   scheduled_date DATE NOT NULL,

   transaction_id BIGINT UNIQUE
       REFERENCES transactions(id)
           ON DELETE SET NULL,

   created_at TIMESTAMPTZ NOT NULL,

   CONSTRAINT uq_recurring_transaction_occurrence
       UNIQUE (recurring_transaction_id, scheduled_date)
);