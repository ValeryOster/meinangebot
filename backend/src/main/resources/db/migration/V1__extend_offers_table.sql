-- Erweiterung der offers-Tabelle für Lidl-Import (und künftige Discounter).
-- Spalten werden nur angelegt, wenn sie noch nicht existieren (idempotent für bestehende DBs).

SET @db := DATABASE();

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'offers' AND COLUMN_NAME = 'brand') = 0,
    'ALTER TABLE offers ADD COLUMN brand VARCHAR(255) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'offers' AND COLUMN_NAME = 'ean') = 0,
    'ALTER TABLE offers ADD COLUMN ean VARCHAR(32) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'offers' AND COLUMN_NAME = 'discount_percent') = 0,
    'ALTER TABLE offers ADD COLUMN discount_percent INT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'offers' AND COLUMN_NAME = 'action_week') = 0,
    'ALTER TABLE offers ADD COLUMN action_week VARCHAR(128) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'offers' AND COLUMN_NAME = 'store_branch') = 0,
    'ALTER TABLE offers ADD COLUMN store_branch VARCHAR(255) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'offers' AND COLUMN_NAME = 'raw_json') = 0,
    'ALTER TABLE offers ADD COLUMN raw_json LONGTEXT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Indizes für Suche und Filterung
SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'offers' AND INDEX_NAME = 'idx_offers_retailer_valid_to') = 0,
    'CREATE INDEX idx_offers_retailer_valid_to ON offers (retailer, valid_to)',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'offers' AND INDEX_NAME = 'idx_offers_category') = 0,
    'CREATE INDEX idx_offers_category ON offers (category(100))',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'offers' AND INDEX_NAME = 'idx_offers_discount') = 0,
    'CREATE INDEX idx_offers_discount ON offers (discount_percent)',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
