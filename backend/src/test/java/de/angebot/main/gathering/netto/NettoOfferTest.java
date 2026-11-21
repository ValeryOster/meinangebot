package de.angebot.main.gathering.netto;

import de.angebot.main.enities.discounters.Netto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Prüft das Auslesen der Netto-Seite anhand eines echten Seitenausschnitts (ohne Netzwerk). */
class NettoOfferTest {

    private static final String PAGE_URL = "https://www.netto-online.de/filialangebote/1/37172";

    private NettoOffer nettoOffer;
    private Document document;

    @BeforeEach
    void setUp() throws IOException {
        nettoOffer = new NettoOffer(null, null, null, "https://www.netto-online.de/filialangebote", "9103");
        try (InputStream html = getClass().getResourceAsStream("/netto/obst-und-gemuese.html")) {
            document = Jsoup.parse(html, StandardCharsets.UTF_8.name(), PAGE_URL);
        }
    }

    @Test
    void parseCategories_usesSubCategoriesAndGroupsWithoutChildren() {
        Map<String, String> categories = nettoOffer.parseCategories(document);

        assertThat(categories).containsExactly(
                Map.entry("https://www.netto-online.de/filialangebote/1/36059", "Knüller der Woche"),
                Map.entry("https://www.netto-online.de/filialangebote/1/37172", "Obst & Gemüse"),
                Map.entry("https://www.netto-online.de/filialangebote/1/37174", "Getränke"),
                Map.entry("https://www.netto-online.de/filialangebote/2/38228", "Super Wochenende"),
                Map.entry("https://www.netto-online.de/filialangebote/4", "Freitag ist Netto-Tag"),
                Map.entry("https://www.netto-online.de/filialangebote/5", "Samstagskracher"));
    }

    @Test
    void parseValidity_readsDatesFromHeadline() {
        LocalDate[] validity = nettoOffer.parseValidity(document);

        assertThat(validity).containsExactly(LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 28));
    }

    @Test
    void parseValidity_singleDay() {
        Document saturday = Jsoup.parse("<h1 class=\"headline__major\">Samstagskracher gültig am Samstag, 28.11.26</h1>");

        assertThat(nettoOffer.parseValidity(saturday))
                .containsExactly(LocalDate.of(2026, 11, 28), LocalDate.of(2026, 11, 28));
    }

    @Test
    void parseOffers_readsAllFieldsOfEveryTile() {
        List<Netto> offers = nettoOffer.parseOffers(document, "Obst & Gemüse", PAGE_URL, List.of("ZESPRI"));

        assertThat(offers).hasSize(3);

        Netto kiwi = offers.get(0);
        assertThat(kiwi.getProduktName()).isEqualTo("Kiwi gold");
        assertThat(kiwi.getProduktMaker()).isEmpty();
        assertThat(kiwi.getProduktPrise()).isEqualTo("0.59");
        assertThat(kiwi.getProduktRegularPrise()).isEqualTo("0.69");
        assertThat(kiwi.getProduktDescription()).isEqualTo("Stück, Italien, Kl. I");
        assertThat(kiwi.getKategorie()).isEqualTo("Obst & Gemüse");
        assertThat(kiwi.getVonDate()).isEqualTo(LocalDate.of(2026, 11, 23));
        assertThat(kiwi.getBisDate()).isEqualTo(LocalDate.of(2026, 11, 28));
        assertThat(kiwi.getUrl()).isEqualTo(PAGE_URL);
        assertThat(kiwi.getImageLink())
                .isEqualTo("https://www.netto-online.de/media_nfs/images/2026-48/40141-450x450-Zespri-Kiwi-gold-2-Stueck-n.webp");

        Netto brokkoli = offers.get(1);
        assertThat(brokkoli.getProduktPrise()).isEqualTo("1.29");
        assertThat(brokkoli.getProduktDescription()).isEqualTo("500 g, 2.58 / kg, Italien / Spanien, Kl. I");

        Netto avocado = offers.get(2);
        assertThat(avocado.getProduktMaker()).isEqualTo("Zespri");
        assertThat(avocado.getProduktName()).isEqualTo("Avocado");
        assertThat(avocado.getProduktRegularPrise()).isEqualTo("1.79");
    }

    @Test
    void parsePrice_handlesNettoFormats() {
        assertThat(NettoOffer.parsePrice("1.11*")).isEqualTo("1.11");
        assertThat(NettoOffer.parsePrice("UVP 1.89")).isEqualTo("1.89");
        assertThat(NettoOffer.parsePrice("statt 1,79")).isEqualTo("1.79");
        assertThat(NettoOffer.parsePrice("–.99")).isEqualTo("0.99");
        assertThat(NettoOffer.parsePrice("")).isEmpty();
    }
}
