CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    amount NUMERIC(38, 2),
    balance_after NUMERIC(38, 2),
    category VARCHAR(255),
    description VARCHAR(255),
    source_statement VARCHAR(255),
    transaction_date DATE,
    type VARCHAR(255),
    CONSTRAINT transactions_type_check CHECK (type IN ('DEBIT', 'CREDIT'))
);