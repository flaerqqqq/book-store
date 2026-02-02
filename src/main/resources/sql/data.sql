INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN'), (2, 'ROLE_EMPLOYEE'), (3, 'ROLE_CLIENT');


INSERT INTO users (id, public_id, email, password, name)
VALUES (1, '00000000-0000-0000-0000-000000000001', 'admin@gmail.com', '$2a$10$AbR3.AyqmNrpfuaCjMj4fOJMkl/YFBAGHQ1OqlyEtm7g2E4wBLUJi', 'Super Admin');
INSERT INTO admins (id) VALUES (1);
INSERT INTO users_roles (user_id, role_id) VALUES (1, 1);


INSERT INTO users (id, public_id, email, password, name)
VALUES (2, '00000000-0000-0000-0000-000000000002', 'employee@gmail.com', '$2a$10$AbR3.AyqmNrpfuaCjMj4fOJMkl/YFBAGHQ1OqlyEtm7g2E4wBLUJi', 'John Staff');
INSERT INTO employees (id, phone, birth_date) VALUES (2, '+380991112233', '1992-08-24');
INSERT INTO users_roles (user_id, role_id) VALUES (2, 2);


INSERT INTO users (id, public_id, email, password, name)
VALUES (3, '00000000-0000-0000-0000-000000000003', 'client@gmail.com', '$2a$10$AbR3.AyqmNrpfuaCjMj4fOJMkl/YFBAGHQ1OqlyEtm7g2E4wBLUJi', 'Ivan Client');
INSERT INTO clients (id, balance) VALUES (3, 1500.50);
INSERT INTO users_roles (user_id, role_id) VALUES (3, 3);
INSERT INTO shopping_carts (public_id, total_amount, client_id) VALUES ('7a1b3c5d-9e8f-4a2b-b1c3-d5e7f9a1b3c5', 0.00, 3);


SELECT setval(pg_get_serial_sequence('users', 'id'), 4, false);


INSERT INTO books (public_id, name, genre, age_group, price, publication_date, author, number_of_pages, characteristics, description, language) VALUES
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

SELECT setval(pg_get_serial_sequence('books', 'id'), 21, false);


INSERT INTO shopping_cart_items (public_id, cart_id, book_id, quantity, price_at_add, subtotal) VALUES
(gen_random_uuid(), 1, 1, 1, 15.99, 15.99),  -- Gatsby
(gen_random_uuid(), 1, 2, 1, 12.50, 12.50),  -- 1984
(gen_random_uuid(), 1, 3, 2, 20.00, 40.00),  -- Hobbit (qty 2)
(gen_random_uuid(), 1, 4, 1, 25.99, 25.99),  -- Harry Potter
(gen_random_uuid(), 1, 5, 1, 30.00, 30.00),  -- Kobzar
(gen_random_uuid(), 1, 6, 1, 11.99, 11.99),  -- Catcher
(gen_random_uuid(), 1, 7, 1, 18.50, 18.50),  -- Dune
(gen_random_uuid(), 1, 8, 1, 10.00, 10.00),  -- Alchemist
(gen_random_uuid(), 1, 9, 1, 14.00, 14.00),  -- Norwegian Wood
(gen_random_uuid(), 1, 10, 1, 13.00, 13.00), -- Brave New World
(gen_random_uuid(), 1, 11, 1, 14.50, 14.50), -- Book Thief
(gen_random_uuid(), 1, 12, 1, 16.00, 16.00), -- Crime and Punishment
(gen_random_uuid(), 1, 13, 1, 8.99, 8.99),   -- Animal Farm
(gen_random_uuid(), 1, 14, 1, 16.50, 16.50), -- Shadow of Wind
(gen_random_uuid(), 1, 15, 3, 11.50, 34.50), -- Little Prince (qty 3)
(gen_random_uuid(), 1, 16, 1, 15.00, 15.00), -- Odyssey
(gen_random_uuid(), 1, 17, 1, 12.00, 12.00), -- Shadows of Ancestors
(gen_random_uuid(), 1, 18, 1, 14.99, 14.99), -- The Road
(gen_random_uuid(), 1, 19, 1, 9.00, 9.00),   -- Intermezzo
(gen_random_uuid(), 1, 20, 1, 17.99, 17.99); -- Anna Karenina
UPDATE shopping_carts SET total_amount = 358.43 WHERE id = 1;