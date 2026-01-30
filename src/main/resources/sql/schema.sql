CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_books_author_trgm
ON books
USING gin(author gin_trgm_ops);