package de.angebot.main.gathering.penny;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@Component
public class PennyApiClient {

    private static final int MAX_ATTEMPTS = 3;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String mainUrl;
    private final List<String> categories;

    public PennyApiClient(@Value("${penny.mainUrl}") String mainUrl,
                          @Value("#{'${penny.categories:top-angebote,obst-und-gemuese,kuehlregal,butchers,angrillen-und-absparen,suessigkeiten-und-snacks,fleisch-und-wurst,weitere-angebote,drogerie-und-haushalt,getraenke,pflanzen-mo-sa,dauerhaft-im-preis-gesenkt,haushalt-und-wohnen,kochen-und-backen,pflanzen,sport-und-freizeit,garten-und-baumarkt,multimedia-und-elektronik,fussballparty,food-highlights-fuer-alle,obst-und-gemuese1,gemeinsam-jubeln-gemeinsam-sparen,xxl-lebensmittel-fuer-alle,sparen-auf-top-marken,drogerie-und-haushalt1,getraenke1,framstag}'.split(',')}")
                          List<String> categories) {
        this.mainUrl = mainUrl;
        this.categories = normalizeCategories(categories);
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<PennyOfferDto> fetchOffers(int year, int week) {
        return categories.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .flatMap(category -> fetchOffers(category, year, week).stream())
                .toList();
    }

    public List<PennyOfferDto> fetchOffers(String category, int year, int week) {
        String yearWeek = String.format("%d-%02d", year, week);
        URI uri = UriComponentsBuilder.fromHttpUrl(mainUrl)
                .pathSegment(".rest", "offers", "by-category", yearWeek, category)
                .build()
                .toUri();

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .GET()
                .build();

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            List<PennyOfferDto> offers = fetchOffers(request, category, yearWeek, attempt);
            if (offers != null) {
                return offers;
            }
        }
        return List.of();
    }

    private List<PennyOfferDto> fetchOffers(HttpRequest request, String category, String yearWeek, int attempt) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                log.error("Penny API category '{}' not found for week '{}'.", category, yearWeek);
                return List.of();
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("Penny API request failed for category '{}' and week '{}' with status {} (attempt {}/{})",
                        category, yearWeek, response.statusCode(), attempt, MAX_ATTEMPTS);
                return attempt == MAX_ATTEMPTS ? List.of() : null;
            }
            return parseOffers(response.body(), category);
        } catch (IOException e) {
            log.error("Penny API request failed for category '{}' and week '{}' (attempt {}/{}): {}",
                    category, yearWeek, attempt, MAX_ATTEMPTS, e.getMessage());
            return attempt == MAX_ATTEMPTS ? List.of() : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Penny API request was interrupted for category '{}' and week '{}'", category, yearWeek);
            return List.of();
        } catch (RuntimeException e) {
            log.error("Penny API response could not be parsed for category '{}' and week '{}' (attempt {}/{}): {}",
                    category, yearWeek, attempt, MAX_ATTEMPTS, e.getMessage());
            return attempt == MAX_ATTEMPTS ? List.of() : null;
        }
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
                    .category(normalizeCategory(readProductCategory(tile, fallbackCategory)))
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

    private List<String> normalizeCategories(List<String> categories) {
        Set<String> normalizedCategories = new LinkedHashSet<>();
        categories.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(this::normalizeCategory)
                .forEach(normalizedCategories::add);
        return List.copyOf(normalizedCategories);
    }

    private String normalizeCategory(String category) {
        return category.replaceFirst("1$", "");
    }
}
