-- ddl.sql

-- Функція для автоматичного оновлення updated_at
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Таблиця для Ролей
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Таблиця для Користувачів
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Таблиця зв'язку користувачів з ролями (багато-до-багатьох)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Таблиця для Місць
CREATE TABLE IF NOT EXISTS locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    description TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Таблиця для Подорожей
CREATE TABLE IF NOT EXISTS journeys (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL, -- Користувач, який створив подорож
    name VARCHAR(255) UNIQUE NOT NULL, -- ДОДАНО: UNIQUE, щоб запобігти дублюванню подорожей з однаковою назвою
    description TEXT,
    start_date DATE,
    end_date DATE,
    origin_location_id BIGINT, -- Зв'язок з таблицею locations
    destination_location_id BIGINT, -- Зв'язок з таблицею locations
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (origin_location_id) REFERENCES locations(id) ON DELETE SET NULL,
    FOREIGN KEY (destination_location_id) REFERENCES locations(id) ON DELETE SET NULL
);

-- Таблиця для Учасників подорожі (багато-до-багатьох зв'язок між користувачами та подорожами)
CREATE TABLE IF NOT EXISTS journey_participants (
    journey_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (journey_id, user_id),
    FOREIGN KEY (journey_id) REFERENCES journeys(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Таблиця для подій (events)
CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    journey_id BIGINT,
    location_id BIGINT,
    name VARCHAR(100) UNIQUE NOT NULL, -- ДОДАНО: UNIQUE, щоб запобігти дублюванню подій з однаковою назвою
    description VARCHAR(500),
    event_date DATE,
    event_time TIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (journey_id) REFERENCES journeys(id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE SET NULL
);

-- Таблиця для Тегів
CREATE TABLE IF NOT EXISTS tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Таблиця зв'язку подорожей з тегами (багато-до-багатьох)
CREATE TABLE IF NOT EXISTS journey_tags (
    journey_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (journey_id, tag_id),
    FOREIGN KEY (journey_id) REFERENCES journeys(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Таблиця для фотографій (photos)
CREATE TABLE IF NOT EXISTS photos (
    id BIGSERIAL PRIMARY KEY,
    journey_id BIGINT,
    user_id BIGINT NOT NULL, -- Користувач, який завантажив фото
    file_path VARCHAR(255) UNIQUE NOT NULL, -- ДОДАНО: UNIQUE, щоб запобігти дублюванню фотографій з однаковим шляхом
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (journey_id) REFERENCES journeys(id) ON DELETE CASCADE, -- Якщо подорож видаляється, фотографії також видаляються
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE -- Якщо користувач видаляється, його фотографії також видаляються
);

-- Додаємо індекси для прискорення пошуку
CREATE INDEX IF NOT EXISTS idx_photos_journey_id ON photos (journey_id);
CREATE INDEX IF NOT EXISTS idx_photos_user_id ON photos (user_id);



-- Тригери для автоматичного оновлення updated_at
CREATE OR REPLACE TRIGGER roles_updated_at
BEFORE UPDATE ON roles
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE OR REPLACE TRIGGER users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE OR REPLACE TRIGGER locations_updated_at
BEFORE UPDATE ON locations
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE OR REPLACE TRIGGER journeys_updated_at
BEFORE UPDATE ON journeys
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE OR REPLACE TRIGGER events_updated_at
BEFORE UPDATE ON events
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE OR REPLACE TRIGGER tags_updated_at
BEFORE UPDATE ON tags
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();
-- Тригери для автоматичного оновлення updated_at
-- ... (існуючі тригери) ...

CREATE OR REPLACE TRIGGER photos_updated_at
BEFORE UPDATE ON photos
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();
