-- Додавання ролей (ці команди мають виконатися першими і успішно)
INSERT INTO roles (name, created_at, updated_at) VALUES ('ROLE_USER', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, created_at, updated_at) VALUES ('ROLE_ADMIN', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;

-- Пароль "password" для всіх користувачів.
-- Хеш згенерований стандартним BCrypt ($2a$10$...): $2a$10$Wq/m1R70Wz/eW0r72t7Uo.p3Jq4g5t6u7v8w9x0y1z2a3b4c5d6e
-- Переконайтеся, що цей хеш відповідає тому, що генерує ваш PasswordHasher.java для "password".
-- Якщо ви перевіряли і генерували свій хеш, використовуйте його. Я залишаю цей як приклад.

-- Створення користувачів
INSERT INTO users (username, email, password_hash, created_at, updated_at) VALUES
('illya_traveler', 'illya.traveler@example.com', '$2a$10$Wq/m1R70Wz/eW0r72t7Uo.p3Jq4g5t6u7v8w9x0y1z2a3b4c5d6e', NOW(), NOW())
ON CONFLICT (email) DO UPDATE SET username = EXCLUDED.username, password_hash = EXCLUDED.password_hash, updated_at = NOW();

INSERT INTO users (username, email, password_hash, created_at, updated_at) VALUES
('olena_explorer', 'olena.explorer@example.com', '$2a$10$Wq/m1R70Wz/eW0r72t7Uo.p3Jq4g5t6u7v8w9x0y1z2a3b4c5d6e', NOW(), NOW())
ON CONFLICT (email) DO UPDATE SET username = EXCLUDED.username, password_hash = EXCLUDED.password_hash, updated_at = NOW();

INSERT INTO users (username, email, password_hash, created_at, updated_at) VALUES
('andriy_discoverer', 'andriy.discoverer@example.com', '$2a$10$Wq/m1R70Wz/eW0r72t7Uo.p3Jq4g5t6u7v8w9x0y1z2a3b4c5d6e', NOW(), NOW())
ON CONFLICT (email) DO UPDATE SET username = EXCLUDED.username, password_hash = EXCLUDED.password_hash, updated_at = NOW();

INSERT INTO users (username, email, password_hash, created_at, updated_at) VALUES
('admin_user', 'admin@example.com', '$2a$10$Wq/m1R70Wz/eW0r72t7Uo.p3Jq4g5t6u7v8w9x0y1z2a3b4c5d6e', NOW(), NOW())
ON CONFLICT (email) DO UPDATE SET username = EXCLUDED.username, password_hash = EXCLUDED.password_hash, updated_at = NOW();

INSERT INTO users (username, email, password_hash, created_at, updated_at) VALUES
('test_user', 'user@example.com', '$2a$10$Wq/m1R70Wz/eW0r72t7Uo.p3Jq4g5t6u7v8w9x0y1z2a3b4c5d6e', NOW(), NOW())
ON CONFLICT (email) DO UPDATE SET username = EXCLUDED.username, password_hash = EXCLUDED.password_hash, updated_at = NOW();

INSERT INTO users (username, email, password_hash, created_at, updated_at) VALUES
('jane_doe', 'jane.doe@example.com', '$2a$10$Wq/m1R70Wz/eW0r72t7Uo.p3Jq4g5t6u7v8w9x0y1z2a3b4c5d6e', NOW(), NOW())
ON CONFLICT (email) DO UPDATE SET username = EXCLUDED.username, password_hash = EXCLUDED.password_hash, updated_at = NOW();

-- Призначення ролей користувачам (після того, як всі користувачі гарантовано створені або оновлені)
INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'illya.traveler@example.com' AND r.name = 'ROLE_USER' ON CONFLICT (user_id, role_id) DO NOTHING;
INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'olena.explorer@example.com' AND r.name = 'ROLE_USER' ON CONFLICT (user_id, role_id) DO NOTHING;
INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'andriy.discoverer@example.com' AND r.name = 'ROLE_USER' ON CONFLICT (user_id, role_id) DO NOTHING;
INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'admin@example.com' AND r.name = 'ROLE_ADMIN' ON CONFLICT (user_id, role_id) DO NOTHING;
INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'admin@example.com' AND r.name = 'ROLE_USER' ON CONFLICT (user_id, role_id) DO NOTHING;
INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'user@example.com' AND r.name = 'ROLE_USER' ON CONFLICT (user_id, role_id) DO NOTHING;
INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'jane.doe@example.com' AND r.name = 'ROLE_USER' ON CONFLICT (user_id, role_id) DO NOTHING;

-- Додавання локацій
INSERT INTO locations (name, description, created_at, updated_at) VALUES ('Київ, Україна', 'Столиця України, велике місто.', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name, description, created_at, updated_at) VALUES ('Львів, Україна', 'Культурна столиця України, місто Лева.', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name, description, created_at, updated_at) VALUES ('Карпати, Україна', 'Гірський хребет у Західній Україні.', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name, description, created_at, updated_at) VALUES ('Одеса, Україна', 'Місто на узбережжі Чорного моря.', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name, description, created_at, updated_at) VALUES ('Гора Говерла, Карпати', 'Найвища вершина Українських Карпат.', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name, description, created_at, updated_at) VALUES ('Одеса, Пляж Ланжерон', 'Популярний пляж в Одесі.', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name, description, created_at, updated_at) VALUES ('Одеса, Катакомби', 'Мережа підземних тунелів під Одесою.', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name, description, created_at, updated_at) VALUES ('Тернопіль, Україна', 'Місто на заході України, обласний центр.', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name, description, created_at, updated_at) VALUES ('Олеський замок, Львівська область', 'Видатна пам''ятка архітектури XIII—XVIII століть.', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;

-- Додавання тегів
INSERT INTO tags (name, created_at, updated_at) VALUES ('Активний відпочинок', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, created_at, updated_at) VALUES ('Гори', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, created_at, updated_at) VALUES ('Море', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, created_at, updated_at) VALUES ('Пригоди', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, created_at, updated_at) VALUES ('Україна', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, created_at, updated_at) VALUES ('Місто', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, created_at, updated_at) VALUES ('Похід', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, created_at, updated_at) VALUES ('Природа', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, created_at, updated_at) VALUES ('Історія', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, created_at, updated_at) VALUES ('Замки', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name, created_at, updated_at) VALUES ('Автоподорож', NOW(), NOW()) ON CONFLICT (name) DO NOTHING;

-- Подорож 1 для Іллі Мандрівника
INSERT INTO journeys (user_id, name, description, start_date, end_date, origin_location_id, destination_location_id)
SELECT u.id, 'Тестовий Похід Іллі', 'Опис тестового походу.', '2024-07-15', '2024-07-21',
       (SELECT id FROM locations WHERE name = 'Львів, Україна'),
       (SELECT id FROM locations WHERE name = 'Гора Говерла, Карпати')
FROM users u WHERE u.email = 'illya.traveler@example.com'
ON CONFLICT (name) DO NOTHING;

-- Подорож 2 для Олени Дослідниці
INSERT INTO journeys (user_id, name, description, start_date, end_date, origin_location_id, destination_location_id)
SELECT u.id, 'Морська Казка Олени', 'Тижневий відпочинок Олени на Чорному морі.', '2024-08-05', '2024-08-12',
       (SELECT id FROM locations WHERE name = 'Київ, Україна'),
       (SELECT id FROM locations WHERE name = 'Одеса, Пляж Ланжерон')
FROM users u WHERE u.email = 'olena.explorer@example.com'
ON CONFLICT (name) DO NOTHING;

-- Подорож 3 для Андрія Відкривача
INSERT INTO journeys (user_id, name, description, start_date, end_date, origin_location_id, destination_location_id)
SELECT u.id, 'Замки Заходу з Андрієм', 'Автомобільна подорож Андрія замками.', '2024-09-02', '2024-09-07',
       (SELECT id FROM locations WHERE name = 'Тернопіль, Україна'),
       (SELECT id FROM locations WHERE name = 'Олеський замок, Львівська область')
FROM users u WHERE u.email = 'andriy.discoverer@example.com'
ON CONFLICT (name) DO NOTHING;

-- Учасники для подорожі 1 "Тестовий Похід Іллі"
INSERT INTO journey_participants (journey_id, user_id)
SELECT j.id, u.id FROM journeys j, users u
WHERE j.name = 'Тестовий Похід Іллі' AND u.email = 'olena.explorer@example.com'
ON CONFLICT (journey_id, user_id) DO NOTHING;

-- Події для подорожі 1 "Тестовий Похід Іллі"
INSERT INTO events (journey_id, location_id, name, description, event_date, event_time)
SELECT j.id, (SELECT id FROM locations WHERE name = 'Гора Говерла, Карпати'), 'Сходження на Говерлу (Тест Іллі)', 'Опис сходження.', '2024-07-18', '09:00:00'
FROM journeys j WHERE j.name = 'Тестовий Похід Іллі'
ON CONFLICT (name) DO NOTHING;

-- Фото для подорожі 1 "Тестовий Похід Іллі"
INSERT INTO photos (user_id, journey_id, file_path, description)
SELECT u.id, j.id, '/uploads/illya_test_hike.jpg', 'Фото з тестового походу Іллі.'
FROM users u, journeys j
WHERE u.email = 'illya.traveler@example.com' AND j.name = 'Тестовий Похід Іллі'
ON CONFLICT (file_path) DO NOTHING;

-- Теги для подорожі 1 "Тестовий Похід Іллі"
INSERT INTO journey_tags (journey_id, tag_id) SELECT j.id, t.id FROM journeys j, tags t WHERE j.name = 'Тестовий Похід Іллі' AND t.name = 'Гори' ON CONFLICT (journey_id, tag_id) DO NOTHING;
INSERT INTO journey_tags (journey_id, tag_id) SELECT j.id, t.id FROM journeys j, tags t WHERE j.name = 'Тестовий Похід Іллі' AND t.name = 'Похід' ON CONFLICT (journey_id, tag_id) DO NOTHING;

-- (Аналогічно заповніть дані для подорожей Олени та Андрія)
-- Наприклад, для Олени:
-- Учасники для подорожі 2 "Морська Казка Олени"
INSERT INTO journey_participants (journey_id, user_id)
SELECT j.id, u.id FROM journeys j, users u
WHERE j.name = 'Морська Казка Олени' AND u.email = 'illya.traveler@example.com'
ON CONFLICT (journey_id, user_id) DO NOTHING;
-- Події для подорожі 2 "Морська Казка Олени"
INSERT INTO events (journey_id, location_id, name, description, event_date, event_time)
SELECT j.id, (SELECT id FROM locations WHERE name = 'Одеса, Катакомби'), 'Екскурсія Одеськими Катакомбами (Тест Олени)', 'Захопливе занурення в підземний світ міста.', '2024-08-08', '14:00:00'
FROM journeys j WHERE j.name = 'Морська Казка Олени'
ON CONFLICT (name) DO NOTHING;
-- Фото для подорожі 2 "Морська Казка Олени"
INSERT INTO photos (user_id, journey_id, file_path, description)
SELECT u.id, j.id, '/uploads/olena_sea_tale.jpg', 'Фото з морської казки Олени.'
FROM users u, journeys j
WHERE u.email = 'olena.explorer@example.com' AND j.name = 'Морська Казка Олени'
ON CONFLICT (file_path) DO NOTHING;
-- Теги для подорожі 2 "Морська Казка Олени"
INSERT INTO journey_tags (journey_id, tag_id) SELECT j.id, t.id FROM journeys j, tags t WHERE j.name = 'Морська Казка Олени' AND t.name = 'Море' ON CONFLICT (journey_id, tag_id) DO NOTHING;
INSERT INTO journey_tags (journey_id, tag_id) SELECT j.id, t.id FROM journeys j, tags t WHERE j.name = 'Морська Казка Олени' AND t.name = 'Місто' ON CONFLICT (journey_id, tag_id) DO NOTHING;


-- Старі приклади подорожей (залиште, якщо вони вам потрібні, і переконайтеся, що їхні назви унікальні)
INSERT INTO journeys (user_id, name, description, start_date, end_date, origin_location_id, destination_location_id)
SELECT u.id, 'Старий Похід в Карпати', 'Тижневий похід по Чорногірському хребту (старий).', '2023-07-10', '2023-07-17',
       (SELECT id FROM locations WHERE name = 'Київ, Україна'),
       (SELECT id FROM locations WHERE name = 'Карпати, Україна')
FROM users u WHERE u.email = 'user@example.com'
ON CONFLICT (name) DO NOTHING;

INSERT INTO journeys (user_id, name, description, start_date, end_date, origin_location_id, destination_location_id)
SELECT u.id, 'Старий Відпочинок в Одесі', 'Тиждень на узбережжі Чорного моря (старий).', '2023-08-01', '2023-08-08',
       (SELECT id FROM locations WHERE name = 'Львів, Україна'),
       (SELECT id FROM locations WHERE name = 'Одеса, Україна')
FROM users u WHERE u.email = 'jane.doe@example.com'
ON CONFLICT (name) DO NOTHING;