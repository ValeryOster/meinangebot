package de.angebot.main.gathering.lidl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LidlLeafletParserTest {

    @Autowired
    private LidlLeafletParser lidlLeafletParser;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void parseFlyer_extractsProductsWithPriceAndPage() throws Exception {
        JsonNode flyerResponse = objectMapper.readTree(readResource("lidl/leaflet-flyer-sample.json"));
        LidlLeafletDescriptor descriptor = LidlLeafletDescriptor.builder()
                .flyerId("019e7426-dba3-7b0f-8f1a-6f123b758fa4")
                .flyerIdentifier("aktionsprospekt-08-06-2026-13-06-2026-d22b0e")
                .name("Aktionsprospekt")
                .title("08.06.2026 – 13.06.2026")
                .category("Filial-Angebote")
                .subcategory("Unsere Aktionsprospekte")
                .status("current")
                .active(true)
                .build();

        var offers = lidlLeafletParser.parseFlyer(flyerResponse, descriptor);

        assertThat(offers).isNotEmpty();
        assertThat(offers.get(0).getProductId()).isNotBlank();
        assertThat(offers.get(0).getCurrentPrice()).isNotNull();
        assertThat(offers.get(0).getLeafletId()).isNotBlank();

        LidlRawOffer rawOffer = lidlLeafletParser.toRawOffer(offers.get(0));
        assertThat(rawOffer.getOfferSource()).isEqualTo(LidlOfferSource.LEAFLET);
    }

    private String readResource(String path) throws Exception {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
