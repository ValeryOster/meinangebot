package de.angebot.main.services;

import de.angebot.main.enities.discounters.Lidl;
import de.angebot.main.enities.offers.Offer;
import de.angebot.main.gathering.lidl.LidlApiClient;
import de.angebot.main.gathering.lidl.LidlHtmlParser;
import de.angebot.main.gathering.lidl.LidlLeafletClient;
import de.angebot.main.gathering.lidl.LidlLeafletParser;
import de.angebot.main.gathering.lidl.LidlRawOffer;
import de.angebot.main.repositories.discounters.LidlRepo;
import de.angebot.main.repositories.offers.OfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LidlImportServiceTest {

    @Mock
    private LidlApiClient lidlApiClient;

    @Mock
    private LidlHtmlParser lidlHtmlParser;

    @Mock
    private LidlLeafletClient lidlLeafletClient;

    @Mock
    private LidlLeafletParser lidlLeafletParser;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private LidlRepo lidlRepo;

    private LidlImportService service;

    @BeforeEach
    void setUp() {
        service = new LidlImportService(lidlApiClient, lidlHtmlParser, lidlLeafletClient, lidlLeafletParser,
                offerRepository, lidlRepo);
    }

    @Test
    void buildExternalId_usesFallbackWhenValidFromMissing() {
        LidlRawOffer rawOffer = LidlRawOffer.builder()
                .productId("1001")
                .canonicalPath("/p/livarno-chair/p1001")
                .campaignUrl("https://www.lidl.de/p/livarno-chair/p1001")
                .leafletId("flyer-123")
                .build();

        String externalId = service.buildExternalId(rawOffer);

        assertThat(externalId).startsWith("lidl-1001-");
        assertThat(externalId).doesNotContain("null");
        assertThat(externalId).isNotEqualTo("lidl-1001-");
    }

    @Test
    void mergeCrossSource_fillsMissingFieldsAndAppendsSource() throws Exception {
        Offer target = new Offer();
        target.setOfferSource("CAMPAIGN");
        target.setTitle("Campaign title");
        target.setCurrentPrice(new BigDecimal("5.99"));
        target.setSourceUrl("https://www.lidl.de/campaign");

        Offer source = new Offer();
        source.setOfferSource("LEAFLET");
        source.setDescription("Leaflet description");
        source.setOriginalPrice(new BigDecimal("7.99"));
        source.setDiscountPercent(25);
        source.setImageUrl("https://cdn.example/image.png");
        source.setValidFrom(LocalDate.of(2026, 6, 8));
        source.setValidTo(LocalDate.of(2026, 6, 13));
        source.setCategory("Filial-Angebote");
        source.setSourceUrl("https://www.lidl.de/leaflet");
        source.setBrand("LIVARNO");
        source.setEan("4012345678901");
        source.setActionWeek("Aktionsprospekt");
        source.setStoreBranch("Filialangebot (Prospekt)");
        source.setRawJson("{\"source\":\"leaflet\"}");
        source.setLeafletId("019e7426-dba3-7b0f-8f1a-6f123b758fa4");
        source.setLeafletPage(4);

        boolean changed = invokeBooleanMethod("mergeCrossSource", target, source);

        assertThat(changed).isTrue();
        assertThat(target.getOfferSource()).isEqualTo("CAMPAIGN,LEAFLET");
        assertThat(target.getTitle()).isEqualTo("Campaign title");
        assertThat(target.getDescription()).isEqualTo("Leaflet description");
        assertThat(target.getOriginalPrice()).isEqualByComparingTo("7.99");
        assertThat(target.getDiscountPercent()).isEqualTo(25);
        assertThat(target.getImageUrl()).isEqualTo("https://cdn.example/image.png");
        assertThat(target.getValidFrom()).isEqualTo(LocalDate.of(2026, 6, 8));
        assertThat(target.getValidTo()).isEqualTo(LocalDate.of(2026, 6, 13));
        assertThat(target.getCategory()).isEqualTo("Filial-Angebote");
        assertThat(target.getSourceUrl()).isEqualTo("https://www.lidl.de/campaign");
        assertThat(target.getBrand()).isEqualTo("LIVARNO");
        assertThat(target.getEan()).isEqualTo("4012345678901");
        assertThat(target.getActionWeek()).isEqualTo("Aktionsprospekt");
        assertThat(target.getStoreBranch()).isEqualTo("Filialangebot (Prospekt)");
        assertThat(target.getRawJson()).isEqualTo("{\"source\":\"leaflet\"}");
        assertThat(target.getLeafletId()).isEqualTo("019e7426-dba3-7b0f-8f1a-6f123b758fa4");
        assertThat(target.getLeafletPage()).isEqualTo(4);
    }

    @Test
    void mergeOffer_overwritesChangedFieldsAndKeepsSingleSourceTag() throws Exception {
        Offer target = new Offer();
        target.setOfferSource("LEAFLET");
        target.setTitle("Old title");
        target.setDescription("Old description");
        target.setCurrentPrice(new BigDecimal("5.99"));
        target.setOriginalPrice(new BigDecimal("9.99"));
        target.setDiscountPercent(10);
        target.setImageUrl("https://cdn.example/old.png");
        target.setValidFrom(LocalDate.of(2026, 6, 1));
        target.setValidTo(LocalDate.of(2026, 6, 6));
        target.setCategory("Old category");
        target.setSourceUrl("https://www.lidl.de/old");
        target.setBrand("OldBrand");
        target.setEan("old-ean");
        target.setActionWeek("Old week");
        target.setStoreBranch("Old branch");
        target.setRawJson("old-json");
        target.setLeafletId("old-leaflet");
        target.setLeafletPage(1);

        Offer source = new Offer();
        source.setOfferSource("LEAFLET");
        source.setTitle("New title");
        source.setDescription("New description");
        source.setCurrentPrice(new BigDecimal("4.99"));
        source.setOriginalPrice(new BigDecimal("8.99"));
        source.setDiscountPercent(20);
        source.setImageUrl("https://cdn.example/new.png");
        source.setValidFrom(LocalDate.of(2026, 6, 8));
        source.setValidTo(LocalDate.of(2026, 6, 13));
        source.setCategory("New category");
        source.setSourceUrl("https://www.lidl.de/new");
        source.setBrand("NewBrand");
        source.setEan("new-ean");
        source.setActionWeek("New week");
        source.setStoreBranch("New branch");
        source.setRawJson("new-json");
        source.setLeafletId("new-leaflet");
        source.setLeafletPage(4);

        boolean changed = invokeBooleanMethod("mergeOffer", target, source);

        assertThat(changed).isTrue();
        assertThat(target.getOfferSource()).isEqualTo("LEAFLET");
        assertThat(target.getTitle()).isEqualTo("New title");
        assertThat(target.getDescription()).isEqualTo("New description");
        assertThat(target.getCurrentPrice()).isEqualByComparingTo("4.99");
        assertThat(target.getOriginalPrice()).isEqualByComparingTo("8.99");
        assertThat(target.getDiscountPercent()).isEqualTo(20);
        assertThat(target.getImageUrl()).isEqualTo("https://cdn.example/new.png");
        assertThat(target.getValidFrom()).isEqualTo(LocalDate.of(2026, 6, 8));
        assertThat(target.getValidTo()).isEqualTo(LocalDate.of(2026, 6, 13));
        assertThat(target.getCategory()).isEqualTo("New category");
        assertThat(target.getSourceUrl()).isEqualTo("https://www.lidl.de/new");
        assertThat(target.getBrand()).isEqualTo("NewBrand");
        assertThat(target.getEan()).isEqualTo("new-ean");
        assertThat(target.getActionWeek()).isEqualTo("New week");
        assertThat(target.getStoreBranch()).isEqualTo("New branch");
        assertThat(target.getRawJson()).isEqualTo("new-json");
        assertThat(target.getLeafletId()).isEqualTo("new-leaflet");
        assertThat(target.getLeafletPage()).isEqualTo(4);
    }

    @Test
    void saveLegacyOffer_updatesExistingRecordFullyWithoutReplacingStoredImage() throws Exception {
        Offer offer = new Offer();
        offer.setSourceUrl("https://www.lidl.de/p/livarno-chair/p1001");
        offer.setTitle("LIVARNO Chair");
        offer.setBrand("LIVARNO");
        offer.setCurrentPrice(new BigDecimal("12.99"));
        offer.setOriginalPrice(new BigDecimal("19.99"));
        offer.setDescription("Sturdy garden chair");
        offer.setValidFrom(LocalDate.of(2026, 6, 8));
        offer.setValidTo(LocalDate.of(2026, 6, 13));
        offer.setCategory("Garten & Balkon");
        offer.setImageUrl("");

        Lidl existing = new Lidl();
        existing.setProduktName("Old name");
        existing.setProduktMaker("Old maker");
        existing.setProduktPrise("1.00");
        existing.setProduktRegularPrise("2.00");
        existing.setProduktDescription("Old description");
        existing.setImageLink("/lidl/2026-06-01/stored-image");
        existing.setVonDate(LocalDate.of(2026, 6, 1));
        existing.setBisDate(LocalDate.of(2026, 6, 6));
        existing.setKategorie("Old category");
        existing.setUrl(offer.getSourceUrl());

        when(lidlRepo.findFirstByUrl(offer.getSourceUrl())).thenReturn(Optional.of(existing));

        invokeSaveLegacyOffer(offer);

        verify(lidlRepo).save(existing);
        assertThat(existing.getProduktName()).isEqualTo("LIVARNO Chair");
        assertThat(existing.getProduktMaker()).isEqualTo("LIVARNO");
        assertThat(existing.getProduktPrise()).isEqualTo("12.99");
        assertThat(existing.getProduktRegularPrise()).isEqualTo("19.99");
        assertThat(existing.getProduktDescription()).isEqualTo("Sturdy garden chair");
        assertThat(existing.getImageLink()).isEqualTo("/lidl/2026-06-01/stored-image");
        assertThat(existing.getVonDate()).isEqualTo(LocalDate.of(2026, 6, 8));
        assertThat(existing.getBisDate()).isEqualTo(LocalDate.of(2026, 6, 13));
        assertThat(existing.getKategorie()).isEqualTo("Garten & Balkon");
        assertThat(existing.getUrl()).isEqualTo(offer.getSourceUrl());
    }

    private boolean invokeBooleanMethod(String methodName, Offer target, Offer source) throws Exception {
        Method method = LidlImportService.class.getDeclaredMethod(methodName, Offer.class, Offer.class);
        method.setAccessible(true);
        return (boolean) method.invoke(service, target, source);
    }

    private void invokeSaveLegacyOffer(Offer offer) throws Exception {
        Method method = LidlImportService.class.getDeclaredMethod("saveLegacyOffer", Offer.class);
        method.setAccessible(true);
        method.invoke(service, offer);
    }
}

