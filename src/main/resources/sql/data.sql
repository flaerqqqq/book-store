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

SELECT setval(pg_get_serial_sequence('users', 'id'), 4, false);