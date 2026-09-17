-- Food Scanner Database Schema

CREATE TABLE IF NOT EXISTS scan_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    food_name TEXT NOT NULL,
    scan_result TEXT NOT NULL,
    health_warnings TEXT,
    health_score INTEGER,
    scanned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_conditions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    condition_name TEXT NOT NULL UNIQUE
);

-- Default health conditions for chain of responsibility checks
INSERT OR IGNORE INTO user_conditions (condition_name) VALUES
    ('diabetes'),
    ('hypertension'),
    ('obesity');
