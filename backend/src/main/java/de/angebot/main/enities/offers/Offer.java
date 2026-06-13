package de.angebot.main.enities.offers;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "offers")
@Data
@NoArgsConstructor
public class Offer {

    @Id
    @Column(length = 80, nullable = false)
    private String externalId;

    @Column(nullable = false, length = 50)
    private String retailer;

    @Column(nullable = false, length = 512)
    private String title;

    @Column(length = 4000)
    private String description;

    private BigDecimal currentPrice;

    private BigDecimal originalPrice;

    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    @Column(length = 1000)
    private String imageUrl;

    private LocalDate validFrom;

    private LocalDate validTo;

    @Column(length = 255)
    private String category;

    @Column(nullable = false, length = 1000)
    private String sourceUrl;

    @Column(length = 255)
    private String brand;

    @Column(length = 32)
    private String ean;

    private Integer discountPercent;

    @Column(length = 128)
    private String actionWeek;

    /** Filialbezug, z. B. „nur in der Filiale“. */
    @Column(length = 255)
    private String storeBranch;

    /** CAMPAIGN oder LEAFLET. */
    @Column(length = 32)
    private String offerSource;

    /** UUID des Prospekts aus der Leaflets-API. */
    @Column(length = 64)
    private String leafletId;

    /** Prospektseite, auf der das Produkt verlinkt ist. */
    private Integer leafletPage;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String rawJson;
}
