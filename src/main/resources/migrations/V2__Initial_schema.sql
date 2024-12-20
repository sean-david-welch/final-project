-- Create base tables
CREATE TABLE users
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    name          VARCHAR(100) NOT NULL,
    role          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER,
    name        VARCHAR(50) NOT NULL UNIQUE,
    type        VARCHAR(35) NOT NULL CHECK (type IN ('FIXED', 'VARIABLE', 'RECURRING', 'EMERGENCY', 'DISCRETIONARY', 'NECESSARY')),
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE budgets
(
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id        INTEGER      NOT NULL,
    name           VARCHAR(100) NOT NULL,
    description    TEXT,
    start_date     DATE,
    end_date       DATE,
    total_income   DECIMAL(10, 2) DEFAULT 0,
    total_expenses DECIMAL(10, 2) DEFAULT 0,
    created_at     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE budget_items
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    budget_id   INTEGER        NOT NULL,
    category_id INTEGER,
    name        VARCHAR(100)   NOT NULL,
    amount      DECIMAL(10, 2) NOT NULL CHECK (amount >= 0),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (budget_id) REFERENCES budgets (id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT
);

CREATE TABLE savings_goals
(
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id        INTEGER        NOT NULL,
    name           VARCHAR(100)   NOT NULL,
    description    TEXT,
    target_amount  DECIMAL(10, 2) NOT NULL,
    current_amount DECIMAL(10, 2) DEFAULT 0,
    target_date    DATE,
    created_at     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE ai_insights
(
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id        INTEGER     NOT NULL,
    budget_id      INTEGER     NOT NULL,
    budget_item_id INTEGER,
    prompt         TEXT        NOT NULL,
    response       TEXT        NOT NULL,
    type           VARCHAR(20) NOT NULL CHECK (type IN ('BUDGET_ANALYSIS', 'ITEM_ANALYSIS', 'SAVING_SUGGESTION',
                                                        'GENERAL_ADVICE')),
    sentiment      VARCHAR(20) CHECK (sentiment IN ('POSITIVE', 'NEGATIVE', 'NEUTRAL')),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata       JSON,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (budget_id) REFERENCES budgets (id) ON DELETE CASCADE,
    FOREIGN KEY (budget_item_id) REFERENCES budget_items (id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_budgets_user ON budgets (user_id);
CREATE INDEX idx_budget_items_budget ON budget_items (budget_id);
CREATE INDEX idx_budget_items_category ON budget_items (category_id);
CREATE INDEX idx_savings_goals_user ON savings_goals (user_id);
CREATE INDEX idx_ai_insights_user ON ai_insights (user_id);
CREATE INDEX idx_ai_insights_budget ON ai_insights (budget_id);
CREATE INDEX idx_ai_insights_item ON ai_insights (budget_item_id);
CREATE INDEX idx_ai_insights_type ON ai_insights (type);
CREATE INDEX idx_ai_insights_date ON ai_insights (created_at);

-- Insert default data
INSERT INTO categories (name, type, description)
VALUES ('Salary', 'FIXED', 'Regular employment income'),
       ('Freelance', 'VARIABLE', 'Freelance or contract work income'),
       ('Investment', 'RECURRING', 'Investment returns and dividends'),
       ('Housing', 'FIXED', 'Rent or mortgage payments'),
       ('Utilities', 'RECURRING', 'Electricity, water, gas, internet'),
       ('Groceries', 'NECESSARY', 'Food and household supplies'),
       ('Transportation', 'FIXED', 'Car payments, fuel, public transit'),
       ('Healthcare', 'NECESSARY', 'Medical expenses and insurance'),
       ('Entertainment', 'DISCRETIONARY', 'Movies, dining out, hobbies'),
       ('Education', 'NECESSARY', 'Courses, books, training materials');