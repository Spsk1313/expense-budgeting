CREATE TABLE transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type VARCHAR(20) NOT NULL
      CHECK (type IN ('INCOME', 'EXPENSE', 'TRANSFER')),
    amount NUMERIC(19, 2) NOT NULL
      CHECK (amount > 0),
    account_id BIGINT REFERENCES accounts(id),
    source_account_id BIGINT REFERENCES accounts(id),
    destination_account_id BIGINT REFERENCES accounts(id),
    category_id BIGINT REFERENCES categories(id),
    description TEXT,
    transaction_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
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
        )
);