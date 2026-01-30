CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE OR REPLACE FUNCTION search_books(name text, author text, descr text, query text)
    RETURNS boolean AS $$
SELECT
    to_tsvector('english', concat_ws(' ', name, author, descr)) @@ plainto_tsquery('english', query)
    OR word_similarity(query, concat_ws(' ', name, author, descr)) > 0.4
$$ LANGUAGE sql;

CREATE INDEX idx_books_author_trgm
ON books
USING gin(author gin_trgm_ops);

CREATE INDEX idx_books_genre_trgm
ON books
USING gin(genre gin_trgm_ops);

CREATE INDEX idx_books_search_trgm
ON books
USING gin(
    (name || ' ' || author || ' ' || description)
    gin_trgm_ops
);