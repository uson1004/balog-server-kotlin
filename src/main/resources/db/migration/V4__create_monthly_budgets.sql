CREATE TABLE monthly_budgets (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    amount BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_monthly_budget_user_month UNIQUE (user_id, year, month),
    CONSTRAINT ck_monthly_budget_amount CHECK (amount >= 0),
    CONSTRAINT ck_monthly_budget_year CHECK (year BETWEEN 2000 AND 2999),
    CONSTRAINT ck_monthly_budget_month CHECK (month BETWEEN 1 AND 12)
);
