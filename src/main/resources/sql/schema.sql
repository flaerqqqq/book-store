CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_books_author_trgm
ON books
USING gin(author gin_trgm_ops);

CREATE INDEX idx_books_genre_trgm
ON books
USING gin(genre gin_trgm_ops);

CREATE INDEX idx_books_search_trgm
ON books
USING gin(
    (
        COALESCE(author, '') || ' ' ||
        COALESCE(name, '') || ' ' ||
        COALESCE(description, '') || ' ' ||
        COALESCE(characteristics, '')
    )
    gin_trgm_ops
);