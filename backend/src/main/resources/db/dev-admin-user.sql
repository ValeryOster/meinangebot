CREATE DATABASE IF NOT EXISTS angebot
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE angebot;

INSERT INTO role (name)
SELECT 'ROLE_USER'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE name = 'ROLE_USER');

INSERT INTO role (name)
SELECT 'ROLE_MODERATOR'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE name = 'ROLE_MODERATOR');

INSERT INTO role (name)
SELECT 'ROLE_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE name = 'ROLE_ADMIN');

INSERT INTO users (username, email, password)
SELECT
  'admin',
  'admin@example.local',
  '$2y$10$HYKgphB11fNfcRhLhReA0OvJEAPh07iRO6n1aOMruTfRnYrG1Sf9m'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

UPDATE users
SET email = 'admin@example.local',
    password = '$2y$10$HYKgphB11fNfcRhLhReA0OvJEAPh07iRO6n1aOMruTfRnYrG1Sf9m'
WHERE username = 'admin';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN role r ON r.name = 'ROLE_USER'
WHERE u.username = 'admin'
  AND NOT EXISTS (
    SELECT 1
    FROM user_roles ur
    WHERE ur.user_id = u.id
      AND ur.role_id = r.id
  );

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN role r ON r.name = 'ROLE_ADMIN'
WHERE u.username = 'admin'
  AND NOT EXISTS (
    SELECT 1
    FROM user_roles ur
    WHERE ur.user_id = u.id
      AND ur.role_id = r.id
  );
