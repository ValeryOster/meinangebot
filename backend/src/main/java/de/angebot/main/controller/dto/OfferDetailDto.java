package de.angebot.main.controller.dto;

import de.angebot.main.enities.offers.Offer;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OfferDetailDto {

    OfferDto offer;
    String rawJson;

    public static OfferDetailDto fromEntity(Offer offer) {
        return OfferDetailDto.builder()
                .offer(OfferDto.fromEntity(offer))
                .rawJson(offer.getRawJson())
                .build();
    }
}
