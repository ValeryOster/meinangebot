package de.angebot.main.gathering.penny;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PennyApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String mainUrl;

    public PennyApiClient(@Value("${penny.mainUrl}") String mainUrl) {
        this.mainUrl = mainUrl;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<PennyOfferDto> fetchOffers(String category, int year, int week) {
        URI uri = UriComponentsBuilder.fromHttpUrl(mainUrl)
                .pathSegment(".rest", "offers", "by-category", year + "-" + week, category)
                .build()
                .toUri();

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("Penny API request failed for category '{}' and week '{}-{}' with status {}",
                        category, year, week, response.statusCode());
                return List.of();
            }
            return parseOffers(response.body(), category);
        } catch (IOException e) {
            log.error("Penny API request failed for category '{}' and week '{}-{}': {}",
                    category, year, week, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Penny API request was interrupted for category '{}' and week '{}-{}'",
                    category, year, week);
        }
        return List.of();
    }

    private List<PennyOfferDto> parseOffers(String body, String fallbackCategory) throws IOException {
        JsonNode offerTiles = objectMapper.readTree(body).path("offerTiles");
        if (!offerTiles.isArray()) {
            return List.of();
        }

        List<PennyOfferDto> offers = new ArrayList<>();
        for (JsonNode tile : offerTiles) {
            String title = text(tile, "title");
            String price = text(tile, "price");
            if (title.isBlank() || price.isBlank() || price.contains("*")) {
                continue;
            }

            offers.add(PennyOfferDto.builder()
                    .title(title.replace("*", "").trim())
                    .price(price)
                    .regularPrice(firstText(tile, "listPrice", "originalPrice", "crossOutPrice"))
                    .imageUrl(firstText(tile.path("imageRendition"), "tileXl", "tileLg", "tileMd", "tileSm", "tileXs"))
                    .category(readProductCategory(tile, fallbackCategory))
                    .linkHref(firstText(tile, "linkHref", "detailLinkHref"))
                    .build());
        }
        return offers;
    }

    private String readProductCategory(JsonNode tile, String fallbackCategory) {
        String productData = text(tile, "productData");
        if (productData.isBlank()) {
            return fallbackCategory;
        }
        try {
            String category = text(objectMapper.readTree(productData), "category");
            return category.isBlank() ? fallbackCategory : category;
        } catch (IOException e) {
            log.error("Penny productData could not be parsed: {}", e.getMessage());
            return fallbackCategory;
        }
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = text(node, field);
            if (!value.isBlank() && !"null".equalsIgnoreCase(value) && !"not_set".equalsIgnoreCase(value)) {
                return value;
            }
        }
        return "";
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("").trim();
    }
}
