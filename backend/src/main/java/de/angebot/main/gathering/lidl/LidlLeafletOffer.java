package de.angebot.main.gathering.lidl;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Zwischenmodell nach dem Parsen eines Lidl-Prospekt-Produkts (Leaflet-API).
 */
@Value
@Builder
public class LidlLeafletOffer {

    String productId;
    String title;
    String description;
    BigDecimal currentPrice;
    String brand;
    String category;
    String imageUrl;
    String canonicalPath;
    String sourceUrl;
    LocalDate validFrom;
    LocalDate validTo;
    String actionWeek;
    Integer leafletPage;
    String leafletId;
    String flyerIdentifier;
    String flyerName;
    String flyerTitle;
    String flyerCategory;
    String rawJson;
}
