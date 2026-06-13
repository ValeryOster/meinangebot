package de.angebot.main.gathering.lidl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LidlHtmlParserTest {

    @Autowired
    private LidlHtmlParser lidlHtmlParser;

    @Autowired
    private LidlApiClient lidlApiClient;

    @Test
    void parseCampaignPage_extractsProductsWithPrice() throws Exception {
        String html = readResource("lidl/campaign-sample.html");

        var offers = lidlHtmlParser.parseCampaignPage(html, "/c/garten-ab-8-6/a10095750");

        assertThat(offers).isNotEmpty();
        assertThat(offers.get(0).getProductId()).isNotBlank();
        assertThat(offers.get(0).getCurrentPrice()).isNotNull();
        assertThat(offers.get(0).getTitle()).isNotBlank();
    }

    @Test
    void discoverCampaignUrls_findsCampaignLinks() throws Exception {
        String html = readResource("lidl/home-sample.html");

        var urls = lidlApiClient.discoverCampaignUrls(html);

        assertThat(urls).isNotEmpty();
        assertThat(urls.iterator().next()).contains("/c/");
    }

    private String readResource(String path) throws Exception {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
