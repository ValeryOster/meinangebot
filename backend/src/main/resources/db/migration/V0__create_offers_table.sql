-- Erstellt die offers-Tabelle, falls sie noch nicht existiert.
-- Idempotent: CREATE TABLE IF NOT EXISTS ist sicher für bestehende Datenbanken.
-- V1 und V2 ergänzen danach die zusätzlichen Spalten.

CREATE TABLE IF NOT EXISTS offers (
    external_id       VARCHAR(80)   NOT NULL,
    retailer          VARCHAR(50)   NOT NULL,
    title             VARCHAR(512)  NOT NULL,
    description       VARCHAR(4000) NULL,
    current_price     DECIMAL(10,2) NULL,
    original_price    DECIMAL(10,2) NULL,
    currency          VARCHAR(3)    NOT NULL DEFAULT 'EUR',
    image_url         VARCHAR(1000) NULL,
    valid_from        DATE          NULL,
    valid_to          DATE          NULL,
    category          VARCHAR(255)  NULL,
    source_url        VARCHAR(1000) NOT NULL,
    PRIMARY KEY (external_id)
);
