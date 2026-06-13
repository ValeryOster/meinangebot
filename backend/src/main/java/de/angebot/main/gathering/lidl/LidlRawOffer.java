package de.angebot.main.gathering.lidl;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Zwischenmodell nach dem Parsen einer Lidl-Datenquelle (Kampagne oder Prospekt).
 */
@Value
@Builder
public class LidlRawOffer {

    String productId;
    String title;
    String description;
    BigDecimal currentPrice;
    BigDecimal originalPrice;
    Integer discountPercent;
    String brand;
    String category;
    String actionWeek;
    String imageUrl;
    String canonicalPath;
    String ean;
    LocalDate validFrom;
    LocalDate validTo;
    String storeBranch;
    String campaignUrl;
    LidlOfferSource offerSource;
    String leafletId;
    Integer leafletPage;
    String rawJson;
}
