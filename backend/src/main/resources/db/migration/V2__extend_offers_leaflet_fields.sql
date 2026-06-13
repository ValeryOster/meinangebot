-- Prospekt-Metadaten für Lidl-Angebote (Angebotsquelle, Prospekt-ID, Seite).
SET @db := DATABASE();

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'offers' AND COLUMN_NAME = 'offer_source') = 0,
    'ALTER TABLE offers ADD COLUMN offer_source VARCHAR(32) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'offers' AND COLUMN_NAME = 'leaflet_id') = 0,
    'ALTER TABLE offers ADD COLUMN leaflet_id VARCHAR(64) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'offers' AND COLUMN_NAME = 'leaflet_page') = 0,
    'ALTER TABLE offers ADD COLUMN leaflet_page INT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'offers' AND INDEX_NAME = 'idx_offers_offer_source') = 0,
    'CREATE INDEX idx_offers_offer_source ON offers (offer_source)',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'offers' AND INDEX_NAME = 'idx_offers_leaflet_id') = 0,
    'CREATE INDEX idx_offers_leaflet_id ON offers (leaflet_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
