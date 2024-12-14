PRAGMA foreign_keys = OFF;

CREATE TABLE categories_new
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER,
    name        VARCHAR(50) NOT NULL,
    type        VARCHAR(35) NOT NULL CHECK (type IN ('FIXED', 'VARIABLE', 'RECURRING', 'EMERGENCY', 'DISCRETIONARY', 'NECESSARY')),
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

INSERT INTO categories_new
SELECT id, user_id, name, type, description, created_at
FROM categories;

DROP TABLE categories;

ALTER TABLE categories_new RENAME TO categories;

PRAGMA foreign_keys = ON;