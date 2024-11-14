-- V1__create_users.sql
CREATE TABLE Users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL,
    age INTEGER NOT NULL
);

-- V2__add_email_to_users.sql
ALTER TABLE Users
ADD COLUMN email VARCHAR(255);