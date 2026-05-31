package de.angebot.main.gathering.penny;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PennyOfferDto {
    private final String title;
    private final String price;
    private final String regularPrice;
    private final String imageUrl;
    private final String category;
    private final String linkHref;
}
