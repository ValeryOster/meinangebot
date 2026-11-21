-- Initiales Schema für PostgreSQL.
-- Ersetzt die früheren MySQL-Migrationen V0–V3 (Umstieg MySQL → PostgreSQL).

-- ---------------------------------------------------------------------------
-- Benutzer und Rollen
-- ---------------------------------------------------------------------------
CREATE TABLE role (
    id   SERIAL      PRIMARY KEY,
    name VARCHAR(20)
);

CREATE TABLE userinfo (
    id               BIGINT       PRIMARY KEY,
    first_name       VARCHAR(255),
    last_name        VARCHAR(255),
    last_singin_date DATE,
    singup_date      DATE,
    time_zone        VARCHAR(255)
);
CREATE SEQUENCE userinfo_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE users (
    id          BIGSERIAL    PRIMARY KEY,
    username    VARCHAR(20)  UNIQUE,
    email       VARCHAR(50)  UNIQUE,
    password    VARCHAR(120),
    userinfo_id BIGINT       REFERENCES userinfo (id)
);

CREATE TABLE user_roles (
    user_id BIGINT  NOT NULL REFERENCES users (id),
    role_id INTEGER NOT NULL REFERENCES role (id),
    PRIMARY KEY (user_id, role_id)
);

-- ---------------------------------------------------------------------------
-- Angebote (einheitliche Tabelle)
-- ---------------------------------------------------------------------------
CREATE TABLE offers (
    external_id      VARCHAR(80)   PRIMARY KEY,
    retailer         VARCHAR(50)   NOT NULL,
    title            VARCHAR(512)  NOT NULL,
    description      VARCHAR(4000),
    brand            VARCHAR(255),
    ean              VARCHAR(32),
    current_price    NUMERIC(10,2),
    original_price   NUMERIC(10,2),
    discount_percent INTEGER,
    currency         VARCHAR(3)    NOT NULL DEFAULT 'EUR',
    image_url        VARCHAR(1000),
    valid_from       DATE,
    valid_to         DATE,
    action_week      VARCHAR(128),
    category         VARCHAR(255),
    store_branch     VARCHAR(255),
    offer_source     VARCHAR(32),
    leaflet_id       VARCHAR(64),
    leaflet_page     INTEGER,
    source_url       VARCHAR(1000) NOT NULL,
    raw_json         TEXT
);
CREATE INDEX idx_offers_retailer_valid_to ON offers (retailer, valid_to);
CREATE INDEX idx_offers_category          ON offers (category);
CREATE INDEX idx_offers_discount          ON offers (discount_percent);
CREATE INDEX idx_offers_offer_source      ON offers (offer_source);
CREATE INDEX idx_offers_leaflet_id        ON offers (leaflet_id);

-- ---------------------------------------------------------------------------
-- Discounter-Tabellen (alt, je Händler)
-- ---------------------------------------------------------------------------
CREATE TABLE aldi (
    id                    BIGINT       PRIMARY KEY,
    von_date              DATE,
    bis_date              DATE,
    kategorie             VARCHAR(255),
    produkt_name          VARCHAR(255),
    produkt_maker         VARCHAR(255),
    produkt_description   VARCHAR(255),
    produkt_prise         VARCHAR(255),
    produkt_regular_prise VARCHAR(255),
    image_link            VARCHAR(255),
    url                   VARCHAR(255)
);
CREATE SEQUENCE aldi_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE edeka (
    id                    BIGINT       PRIMARY KEY,
    von_date              DATE,
    bis_date              DATE,
    kategorie             VARCHAR(255),
    produkt_name          VARCHAR(255),
    produkt_maker         VARCHAR(255),
    produkt_description   VARCHAR(255),
    produkt_prise         VARCHAR(255),
    produkt_regular_prise VARCHAR(255),
    image_link            VARCHAR(255),
    url                   VARCHAR(255)
);
CREATE SEQUENCE edeka_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE netto (
    id                    BIGINT       PRIMARY KEY,
    von_date              DATE,
    bis_date              DATE,
    kategorie             VARCHAR(255),
    produkt_name          VARCHAR(255),
    produkt_maker         VARCHAR(255),
    produkt_description   VARCHAR(255),
    produkt_prise         VARCHAR(255),
    produkt_regular_prise VARCHAR(255),
    image_link            VARCHAR(255),
    url                   VARCHAR(255)
);
CREATE SEQUENCE netto_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE penny (
    id                    BIGINT       PRIMARY KEY,
    von_date              DATE,
    bis_date              DATE,
    kategorie             VARCHAR(255),
    produkt_name          VARCHAR(255),
    produkt_maker         VARCHAR(255),
    produkt_prise         VARCHAR(255),
    produkt_regular_prise VARCHAR(255),
    image_link            VARCHAR(255),
    url                   VARCHAR(255)
);
CREATE SEQUENCE penny_seq START WITH 1 INCREMENT BY 50;

-- ---------------------------------------------------------------------------
-- Hersteller, Auswahl der Benutzer, Sammel-Läufe
-- ---------------------------------------------------------------------------
CREATE TABLE product_maker (
    id         BIGINT       PRIMARY KEY,
    maker_name VARCHAR(255) NOT NULL UNIQUE,
    valid      BOOLEAN
);
CREATE SEQUENCE product_maker_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE selected_item (
    id              BIGINT       PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    item_id         BIGINT       NOT NULL,
    discounter_name VARCHAR(255),
    save_date       DATE,
    expiry_date     DATE
);
CREATE SEQUENCE selected_item_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE common_gather (
    id          BIGINT PRIMARY KEY,
    gather_date DATE,
    expiry_date DATE,
    duration    BIGINT
);
CREATE SEQUENCE common_gather_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE common_gather_discount (
    common_gather_id BIGINT       NOT NULL REFERENCES common_gather (id),
    discount_key     VARCHAR(255) NOT NULL,
    discount         VARCHAR(255),
    PRIMARY KEY (common_gather_id, discount_key)
);
