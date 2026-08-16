CREATE TABLE budgets (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) NOT NULL,
    category_id BIGINT REFERENCES categories(id) NOT NULL,
    month DATE NOT NULL CHECK ( DATE_TRUNC('month', month)::date = month ),
    limit_amount NUMERIC(19, 2) NOT NULL CHECK ( limit_amount > 0 ),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (user_id, category_id, month)
);