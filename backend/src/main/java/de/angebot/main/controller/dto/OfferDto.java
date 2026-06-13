package de.angebot.main.controller.dto;

import de.angebot.main.enities.offers.Offer;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class OfferDto {

    String externalId;
    String retailer;
    String title;
    String description;
    BigDecimal currentPrice;
    BigDecimal originalPrice;
    String currency;
    String imageUrl;
    LocalDate validFrom;
    LocalDate validTo;
    String category;
    String sourceUrl;
    String brand;
    String ean;
    Integer discountPercent;
    String actionWeek;
    String storeBranch;
    String offerSource;
    String leafletId;
    Integer leafletPage;

    public static OfferDto fromEntity(Offer offer) {
        return OfferDto.builder()
                .externalId(offer.getExternalId())
                .retailer(offer.getRetailer())
                .title(offer.getTitle())
                .description(offer.getDescription())
                .currentPrice(offer.getCurrentPrice())
                .originalPrice(offer.getOriginalPrice())
                .currency(offer.getCurrency())
                .imageUrl(offer.getImageUrl())
                .validFrom(offer.getValidFrom())
                .validTo(offer.getValidTo())
                .category(offer.getCategory())
                .sourceUrl(offer.getSourceUrl())
                .brand(offer.getBrand())
                .ean(offer.getEan())
                .discountPercent(offer.getDiscountPercent())
                .actionWeek(offer.getActionWeek())
                .storeBranch(offer.getStoreBranch())
                .offerSource(offer.getOfferSource())
                .leafletId(offer.getLeafletId())
                .leafletPage(offer.getLeafletPage())
                .build();
    }
}
