CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_books_fts
ON books
USING gin(to_tsvector('english',
        coalesce(name, '') || ' ' ||
        coalesce(author, '') || ' ' ||
        coalesce(description, '')
));

CREATE INDEX idx_books_author_trgm
ON books
USING gin(author gin_trgm_ops);

CREATE INDEX idx_books_genre_trgm
ON books
USING gin(genre gin_trgm_ops);

CREATE INDEX idx_books_search_trgm
ON books
USING gin(
      (coalesce(name, '') || ' ' || coalesce(author, '') || ' ' || coalesce(description, ''))
      gin_trgm_ops
);

CREATE OR REPLACE FUNCTION search_books(name text, author text, descr text, query text)
    RETURNS boolean AS $$
SELECT
        to_tsvector('english', coalesce(name, '') || ' ' || coalesce(author, '') || ' ' || coalesce(descr, ''))
        @@ plainto_tsquery('english', query)
        OR word_similarity(query, coalesce(name, '') || ' ' || coalesce(author, '') || ' ' || coalesce(descr, '')) > 0.4
$$ LANGUAGE sql IMMUTABLE;

DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS shopping_cart_items;
DROP TABLE IF EXISTS shopping_carts;
DROP TABLE IF EXISTS users_roles;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS admins;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS clients;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS books;

CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    genre VARCHAR(255) NOT NULL,
    age_group VARCHAR(50) NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    publication_date DATE NOT NULL,
    author VARCHAR(255) NOT NULL,
    number_of_pages INTEGER NOT NULL,
    characteristics TEXT NOT NULL,
    description TEXT NOT NULL,
    language VARCHAR(50)
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE clients (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00
);

CREATE TABLE employees (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    phone VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL
);

CREATE TABLE admins (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE shopping_carts (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    total_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    client_id BIGINT NOT NULL UNIQUE REFERENCES clients(id)
);

CREATE TABLE shopping_cart_items (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    cart_id BIGINT NOT NULL REFERENCES shopping_carts(id),
    book_id BIGINT NOT NULL REFERENCES books(id),
    quantity INTEGER NOT NULL,
    price_at_add DECIMAL(19, 2) NOT NULL,
    subtotal DECIMAL(19, 2) NOT NULL
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    client_id BIGINT NOT NULL REFERENCES clients(id),
    employee_id BIGINT REFERENCES employees(id),
    order_date TIMESTAMP NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    delivery_type VARCHAR(50) NOT NULL,
    delivery_address VARCHAR(255),
    comment TEXT,
    status VARCHAR(50) NOT NULL,
    cancelled_by BIGINT REFERENCES users(id),
    reason TEXT,
    cancelled_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    book_public_id UUID NOT NULL,
    book_name VARCHAR(255) NOT NULL,
    price_at_purchase DECIMAL(19, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    subtotal DECIMAL(19, 2) NOT NULL
);