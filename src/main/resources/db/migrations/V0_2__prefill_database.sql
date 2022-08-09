-- Create user and admin roles which are necessary for app initialization
INSERT INTO main.role (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO main.role (id, name) VALUES (2, 'ROLE_ADMIN');