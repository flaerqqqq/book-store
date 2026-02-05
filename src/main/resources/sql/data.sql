INSERT INTO roles (id, name) VALUES
    (1, 'ROLE_ADMIN'),
    (2, 'ROLE_EMPLOYEE'),
    (3, 'ROLE_CLIENT');

INSERT INTO users (id, public_id, email, password, name) VALUES
    (1, 'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'admin@gmail.com', '$2a$10$AbR3.AyqmNrpfuaCjMj4fOJMkl/YFBAGHQ1OqlyEtm7g2E4wBLUJi', 'Super Admin'),
    (2, 'c9bf9e57-1685-4c89-bafb-ff5af830be8a', 'employee@gmail.com', '$2a$10$AbR3.AyqmNrpfuaCjMj4fOJMkl/YFBAGHQ1OqlyEtm7g2E4wBLUJi', 'John Staff'),
    (3, '2ba82d8c-2831-4ec3-93d4-651630132da9', 'client@gmail.com', '$2a$10$AbR3.AyqmNrpfuaCjMj4fOJMkl/YFBAGHQ1OqlyEtm7g2E4wBLUJi', 'Ivan Client'),
    (4, 'e82c1d9a-3742-4f11-b2c3-d5e7f9a1b3c5', 'employee2@gmail.com', '$2a$10$AbR3.AyqmNrpfuaCjMj4fOJMkl/YFBAGHQ1OqlyEtm7g2E4wBLUJi', 'Jane Manager');

INSERT INTO admins (id) VALUES (1);

INSERT INTO employees (id, phone, birth_date) VALUES
(2, '+380991112233', '1992-08-24'),
(4, '+380509998877', '1988-11-12');

INSERT INTO clients (id, balance) VALUES
    (3, 1500.50);

INSERT INTO users_roles (user_id, role_id) VALUES
    (1, 1), (2, 2), (3, 3), (4, 2);

INSERT INTO shopping_carts (public_id, total_amount, client_id) VALUES
    ('7a1b3c5d-9e8f-4a2b-b1c3-d5e7f9a1b3c5', 0.00, 3);

INSERT INTO books (public_id, name, genre, age_group, price, publication_date, author, number_of_pages, characteristics, description, language)
VALUES
    (gen_random_uuid(), 'The Great Gatsby', 'Classic', 'ADULT', 15.99, '1925-04-10', 'F. Scott Fitzgerald', 180, 'Hardcover', 'A story of wealth and love.', 'ENGLISH'),
    (gen_random_uuid(), '1984', 'Dystopian', 'ADULT', 12.50, '1949-06-08', 'George Orwell', 328, 'Paperback', 'Big Brother is watching you.', 'ENGLISH'),
    (gen_random_uuid(), 'The Hobbit', 'Fantasy', 'CHILD', 20.00, '1937-09-21', 'J.R.R. Tolkien', 310, 'Illustrated', 'A hobbits unexpected journey.', 'ENGLISH'),
    (gen_random_uuid(), 'Harry Potter and the Sorcerers Stone', 'Fantasy', 'CHILD', 25.99, '1997-06-26', 'J.K. Rowling', 309, 'Hardcover', 'The boy who lived.', 'ENGLISH'),
    (gen_random_uuid(), 'Kobzar', 'Poetry', 'ADULT', 30.00, '1840-04-26', 'Taras Shevchenko', 115, 'Leather Bound', 'Foundational Ukrainian literature.', 'UKRAINIAN'),
    (gen_random_uuid(), 'The Catcher in the Rye', 'Fiction', 'TEEN', 11.99, '1951-07-16', 'J.D. Salinger', 214, 'Softcover', 'Holden Caulfields teenage angst.', 'ENGLISH'),
    (gen_random_uuid(), 'Dune', 'Sci-Fi', 'ADULT', 18.50, '1965-08-01', 'Frank Herbert', 412, 'Hardcover', 'The spice must flow.', 'ENGLISH'),
    (gen_random_uuid(), 'The Alchemist', 'Adventure', 'ADULT', 10.00, '1988-05-01', 'Paulo Coelho', 167, 'Paperback', 'Follow your personal legend.', 'SPANISH'),
    (gen_random_uuid(), 'Norwegian Wood', 'Fiction', 'ADULT', 14.00, '1987-09-04', 'Haruki Murakami', 296, 'Softcover', 'A story of loss and sexuality.', 'JAPANESE'),
    (gen_random_uuid(), 'Brave New World', 'Dystopian', 'ADULT', 13.00, '1932-10-01', 'Aldous Huxley', 268, 'Paperback', 'A futuristic society.', 'ENGLISH'),
    (gen_random_uuid(), 'The Book Thief', 'Historical', 'TEEN', 14.50, '2005-03-14', 'Markus Zusak', 552, 'Hardcover', 'Death tells a story.', 'GERMAN'),
    (gen_random_uuid(), 'Crime and Punishment', 'Psychological', 'ADULT', 16.00, '1866-01-01', 'Fyodor Dostoevsky', 671, 'Hardcover', 'A student commits a murder.', 'OTHER'),
    (gen_random_uuid(), 'Animal Farm', 'Satire', 'ADULT', 8.99, '1945-08-17', 'George Orwell', 112, 'Paperback', 'All animals are equal.', 'ENGLISH'),
    (gen_random_uuid(), 'The Shadow of the Wind', 'Mystery', 'ADULT', 16.50, '2001-04-01', 'Carlos Ruiz Zafón', 487, 'Hardcover', 'A boy discovers a secret library.', 'SPANISH'),
    (gen_random_uuid(), 'The Little Prince', 'Fable', 'CHILD', 11.50, '1943-04-06', 'Antoine de Saint-Exupéry', 96, 'Illustrated', 'A pilot meets a prince.', 'FRENCH'),
    (gen_random_uuid(), 'The Odyssey', 'Epic', 'ADULT', 15.00, '0800-01-01', 'Homer', 541, 'Hardcover', 'Odysseus travels home.', 'OTHER'),
    (gen_random_uuid(), 'Shadows of Forgotten Ancestors', 'Novella', 'ADULT', 12.00, '1911-01-01', 'Mykhailo Kotsiubynsky', 160, 'Paperback', 'A tale of Carpathian Hutsuls.', 'UKRAINIAN'),
    (gen_random_uuid(), 'The Road', 'Post-Apocalyptic', 'ADULT', 14.99, '2006-09-26', 'Cormac McCarthy', 287, 'Paperback', 'A father and son survive.', 'ENGLISH'),
    (gen_random_uuid(), 'Intermezzo', 'Novella', 'ADULT', 9.00, '1908-01-01', 'Mykhailo Kotsiubynsky', 80, 'Pocket Edition', 'A story of psychological fatigue.', 'UKRAINIAN'),
    (gen_random_uuid(), 'Anna Karenina', 'Realism', 'ADULT', 17.99, '1877-01-01', 'Leo Tolstoy', 864, 'Hardcover', 'A tragic love affair.', 'OTHER');

INSERT INTO orders
(id, public_id, client_id, employee_id, order_date, total_amount, delivery_type, delivery_address, comment, status, cancelled_by, reason, cancelled_at, version)
VALUES
    (1, gen_random_uuid(), 3, 2, '2026-02-01 10:30:00', 48.49, 'STANDARD', '123 Kyiv St, Rivne', 'Please leave at the door', 'COMPLETED', NULL, NULL, NULL, 1),
    (2, gen_random_uuid(), 3, NULL, '2026-02-04 15:00:00', 30.00, 'PICKUP', 'Store Branch A', 'I will pick it up after work', 'CREATED', NULL, NULL, NULL, 0),
    (3, gen_random_uuid(), 3, 2, '2026-02-02 09:00:00', 12.50, 'STANDARD', '123 Kyiv St, Rivne', 'Changed my mind', 'CANCELLED', 3, 'User decided to buy a different edition', '2026-02-02 11:30:00', 2),
    (4, gen_random_uuid(), 3, 4, '2026-02-05 10:00:00', 20.00, 'PICKUP', NULL, 'Will pick up at main office', 'CONFIRMED', NULL, NULL, NULL, 1),
    (5, gen_random_uuid(), 3, 4, '2026-02-05 12:00:00', 41.50, 'EXPRESS', '45 Soborna St, Rivne', 'Gift wrapping requested', 'SHIPPED', NULL, NULL, NULL, 1),
    (6, gen_random_uuid(), 3, 2, '2026-02-05 14:00:00', 18.50, 'STANDARD', '123 Kyiv St, Rivne', 'Processing by John', 'CLAIMED', NULL, NULL, NULL, 1),
    (7, gen_random_uuid(), 3, 4, '2026-02-05 14:15:00', 16.00, 'PICKUP', NULL, 'Processing by Jane', 'CLAIMED', NULL, NULL, NULL, 1),
    (8, gen_random_uuid(), 3, NULL, '2026-02-05 14:30:00', 12.50, 'STANDARD', '77 Central Ave', 'New unassigned order', 'CREATED', NULL, NULL, NULL, 0),
    (9, gen_random_uuid(), 3, 2, '2026-02-05 14:45:00', 8.99, 'PICKUP', NULL, 'John is fast on this one', 'SHIPPED', NULL, NULL, NULL, 1),
    (10, gen_random_uuid(), 3, 4, '2026-02-05 15:00:00', 15.99, 'EXPRESS', '12 Myru St', 'Jane handled express', 'COMPLETED', NULL, NULL, NULL, 1),
    (11, gen_random_uuid(), 3, NULL, '2026-02-05 15:15:00', 25.99, 'STANDARD', '99 Green St', 'Wait for claim', 'CREATED', NULL, NULL, NULL, 0),
    (12, gen_random_uuid(), 3, 4, '2026-02-05 15:30:00', 11.99, 'PICKUP', NULL, 'Jane personal task', 'CLAIMED', NULL, NULL, NULL, 1),
    (13, gen_random_uuid(), 3, 2, '2026-02-05 15:45:00', 14.50, 'EXPRESS', '101 Tech Way', 'John urgent delivery', 'CONFIRMED', NULL, NULL, NULL, 1);

INSERT INTO order_items
(order_id, book_public_id, book_name, price_at_purchase, quantity, subtotal)
VALUES
    (1, (SELECT public_id FROM books WHERE name = 'The Great Gatsby'), 'The Great Gatsby', 15.99, 1, 15.99),
    (1, (SELECT public_id FROM books WHERE name = '1984'), '1984', 12.50, 2, 25.00),
    (2, (SELECT public_id FROM books WHERE name = 'Kobzar'), 'Kobzar', 30.00, 1, 30.00),
    (3, (SELECT public_id FROM books WHERE name = '1984'), '1984', 12.50, 1, 12.50),
    (4, (SELECT public_id FROM books WHERE name = 'The Hobbit'), 'The Hobbit', 20.00, 1, 20.00),
    (5, (SELECT public_id FROM books WHERE name = 'Harry Potter and the Sorcerers Stone'), 'Harry Potter and the Sorcerers Stone', 25.99, 1, 25.99),
    (6, (SELECT public_id FROM books WHERE name = 'Dune'), 'Dune', 18.50, 1, 18.50),
    (7, (SELECT public_id FROM books WHERE name = 'Crime and Punishment'), 'Crime and Punishment', 16.00, 1, 16.00),
    (8, (SELECT public_id FROM books WHERE name = '1984'), '1984', 12.50, 1, 12.50),
    (9, (SELECT public_id FROM books WHERE name = 'Animal Farm'), 'Animal Farm', 8.99, 1, 8.99),
    (10, (SELECT public_id FROM books WHERE name = 'The Great Gatsby'), 'The Great Gatsby', 15.99, 1, 15.99),
    (11, (SELECT public_id FROM books WHERE name = 'Harry Potter and the Sorcerers Stone'), 'Harry Potter and the Sorcerers Stone', 25.99, 1, 25.99),
    (12, (SELECT public_id FROM books WHERE name = 'The Catcher in the Rye'), 'The Catcher in the Rye', 11.99, 1, 11.99),
    (13, (SELECT public_id FROM books WHERE name = 'The Book Thief'), 'The Book Thief', 14.50, 1, 14.50);

SELECT setval('public.orders_id_seq', COALESCE((SELECT MAX(id) FROM public.orders), 1));
SELECT setval('public.order_items_id_seq', COALESCE((SELECT MAX(id) FROM public.order_items), 1));
SELECT setval('public.orders_id_seq', COALESCE((SELECT MAX(id) FROM public.orders), 1));
SELECT setval('public.order_items_id_seq', COALESCE((SELECT MAX(id) FROM public.order_items), 1));
SELECT setval('public.users_id_seq', COALESCE((SELECT MAX(id) FROM public.users), 1));
SELECT setval('public.books_id_seq', COALESCE((SELECT MAX(id) FROM public.books), 1));
SELECT setval('public.orders_id_seq', COALESCE((SELECT MAX(id) FROM public.orders), 1));
SELECT setval('public.order_items_id_seq', COALESCE((SELECT MAX(id) FROM public.order_items), 1));
SELECT setval('public.shopping_carts_id_seq', COALESCE((SELECT MAX(id) FROM public.shopping_carts), 1));
SELECT setval('public.shopping_cart_items_id_seq', COALESCE((SELECT MAX(id) FROM public.shopping_cart_items), 1));
SELECT setval('public.roles_id_seq', COALESCE((SELECT MAX(id) FROM public.roles), 1));