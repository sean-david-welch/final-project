PRAGMA foreign_keys=off;

BEGIN TRANSACTION;

-- Create new table without the unique constraint
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

-- Copy existing data
INSERT INTO categories_new
SELECT id, user_id, name, type, description, created_at
FROM categories;

-- Drop old table
DROP TABLE categories;

-- Rename new table to original name
ALTER TABLE categories_new RENAME TO categories;

COMMIT;

PRAGMA foreign_keys=on;