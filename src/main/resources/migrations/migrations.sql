-- V1__create_users.sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- V2__create_categories.sql
CREATE TABLE categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('EXPENSE', 'INCOME')),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- V3__create_budgets.sql
CREATE TABLE budgets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_income DECIMAL(10,2) DEFAULT 0,
    total_expenses DECIMAL(10,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- V4__create_budget_items.sql
CREATE TABLE budget_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    budget_id INTEGER NOT NULL,
    category_id INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (budget_id) REFERENCES budgets(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

-- V5__create_savings_goals.sql
CREATE TABLE savings_goals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    target_amount DECIMAL(10,2) NOT NULL,
    current_amount DECIMAL(10,2) DEFAULT 0,
    target_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- V6__create_ai_insights.sql
CREATE TABLE ai_insights (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    budget_id INTEGER NOT NULL,
    budget_item_id INTEGER,
    prompt TEXT NOT NULL,
    response TEXT NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('BUDGET_ANALYSIS', 'ITEM_ANALYSIS', 'SAVING_SUGGESTION', 'GENERAL_ADVICE')),
    sentiment VARCHAR(20) CHECK (sentiment IN ('POSITIVE', 'NEGATIVE', 'NEUTRAL')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSON,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (budget_id) REFERENCES budgets(id) ON DELETE CASCADE,
    FOREIGN KEY (budget_item_id) REFERENCES budget_items(id) ON DELETE CASCADE
);

-- V7__create_indexes.sql
CREATE INDEX idx_budgets_user ON budgets(user_id);
CREATE INDEX idx_budget_items_budget ON budget_items(budget_id);
CREATE INDEX idx_budget_items_category ON budget_items(category_id);
CREATE INDEX idx_savings_goals_user ON savings_goals(user_id);
CREATE INDEX idx_ai_insights_user ON ai_insights(user_id);
CREATE INDEX idx_ai_insights_budget ON ai_insights(budget_id);
CREATE INDEX idx_ai_insights_item ON ai_insights(budget_item_id);
CREATE INDEX idx_ai_insights_type ON ai_insights(type);
CREATE INDEX idx_ai_insights_date ON ai_insights(created_at);

-- V8__enable_foreign_keys_and_wal.sql
PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;

-- V9__insert_default_categories.sql
INSERT INTO categories (name, type, description) VALUES
    ('Salary', 'INCOME', 'Regular employment income'),
    ('Freelance', 'INCOME', 'Freelance or contract work income'),
    ('Investment', 'INCOME', 'Investment returns and dividends'),
    ('Housing', 'EXPENSE', 'Rent or mortgage payments'),
    ('Utilities', 'EXPENSE', 'Electricity, water, gas, internet'),
    ('Groceries', 'EXPENSE', 'Food and household supplies'),
    ('Transportation', 'EXPENSE', 'Car payments, fuel, public transit'),
    ('Healthcare', 'EXPENSE', 'Medical expenses and insurance'),
    ('Entertainment', 'EXPENSE', 'Movies, dining out, hobbies'),
    ('Education', 'EXPENSE', 'Courses, books, training materials');