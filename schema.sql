CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
CREATE TABLE categories(
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    color_hex VARCHAR(7) DEFAULT '#8888888',
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(name, user_id)
);

CREATE TABLE expenses(
    id SERIAL PRIMARY KEY,
    amount NUMERIC(10,2) NOT NULL CHECK(amount>0),
    description VARCHAR(255),
    category_id INT REFERENCES categories(id) ON DELETE SET NULL,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expense_date DATE DEFAULT CURRENT_DATE,
    created_at TIMESTAMP DEFAULT NOW()
);

INSERT INTO users (username, email, password_hash)
VALUES ('demo', 'demo@example.com', 'changeme');

INSERT INTO categories(name, color_hex, user_id)
VALUES
('Food',          '#A8DF8E', 1),
('Transport',     '#F0FFDF', 1),
('Utilities',     '#FFD8DF', 1),
('Cosmetics', '#FFAAB8', 1),
('Clothing', '#F9F6C4',  1),
('Entertainment', '#44ACFF', 1),
('Other', '#EDFFF0', 1)