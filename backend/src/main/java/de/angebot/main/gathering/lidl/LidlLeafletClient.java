package de.angebot.main.gathering.lidl;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * HTTP-Client für die Schwarz-Group Leaflets-API (Lidl-Prospekte).
 * Dokumentiert unter {@code endpoints.leaflets.schwarz/v4/*} – öffentlich, kein Auth.
 */
@Slf4j
@Component
public class LidlLeafletClient {

    private static final int MAX_ATTEMPTS = 3;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiBaseUrl;
    private final String clientLocale;
    private final String countryCode;
    private final String language;
    private final Set<String> includedCategories;
    private final String userAgent;

    public LidlLeafletClient(ObjectMapper objectMapper,
                             @Value("${lidl.leaflet.apiBaseUrl:https://endpoints.leaflets.schwarz}") String apiBaseUrl,
                             @Value("${lidl.leaflet.clientLocale:lidl/de-DE}") String clientLocale,
                             @Value("${lidl.leaflet.countryCode:DE}") String countryCode,
                             @Value("${lidl.leaflet.language:de}") String language,
                             @Value("${lidl.leaflet.includeCategories:Filial-Angebote,Onlineshop-Angebote}") String includeCategories,
                             @Value("${lidl.userAgent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                                     + "(KHTML, like Gecko) Chrome/125.0 Safari/537.36}") String userAgent) {
        this.objectMapper = objectMapper;
        this.apiBaseUrl = trimTrailingSlash(apiBaseUrl);
        this.clientLocale = clientLocale;
        this.countryCode = countryCode;
        this.language = language;
        this.includedCategories = java.util.Arrays.stream(includeCategories.split(","))
                .map(String::trim)
                .filter(org.springframework.util.StringUtils::hasText)
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toSet());
        this.userAgent = userAgent;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Lädt die Prospekt-Übersicht und liefert deduplizierte, aktive Prospekte.
     * Regionale Varianten (gleicher Name/Titel) werden auf einen Eintrag reduziert.
     */
    public List<LidlLeafletDescriptor> discoverFlyers() {
        JsonNode overview = fetchOverview();
        if (overview == null || !overview.path("success").asBoolean(false)) {
            log.error("Lidl leaflet overview could not be loaded.");
            return List.of();
        }

        Map<String, LidlLeafletDescriptor> uniqueFlyers = new LinkedHashMap<>();
        for (JsonNode categoryNode : overview.path("categories")) {
            String category = text(categoryNode, "name");
            if (!isIncludedCategory(category)) {
                continue;
            }
            for (JsonNode subcategoryNode : categoryNode.path("subcategories")) {
                String subcategory = text(subcategoryNode, "name");
                for (JsonNode flyerNode : subcategoryNode.path("flyers")) {
                    if (!flyerNode.path("isActive").asBoolean(false)) {
                        continue;
                    }
                    String status = text(flyerNode, "status");
                    if (!"current".equalsIgnoreCase(status) && !"upcoming".equalsIgnoreCase(status)) {
                        continue;
                    }
                    LidlLeafletDescriptor descriptor = toDescriptor(flyerNode, category, subcategory, status);
                    if (descriptor == null || !StringUtils.hasText(descriptor.getFlyerIdentifier())) {
                        continue;
                    }
                    String dedupeKey = descriptor.getName() + "|" + descriptor.getTitle() + "|" + subcategory;
                    uniqueFlyers.putIfAbsent(dedupeKey, descriptor);
                }
            }
        }

        log.info("Lidl leaflet discovery found {} unique flyers (from overview).", uniqueFlyers.size());
        return new ArrayList<>(uniqueFlyers.values());
    }

    public JsonNode fetchFlyer(String flyerIdentifier) {
        URI uri = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/v4/flyer")
                .queryParam("flyer_identifier", flyerIdentifier)
                .queryParam("country_code", countryCode)
                .queryParam("language", language)
                .build()
                .toUri();
        return fetchJson(uri, "flyer " + flyerIdentifier);
    }

    private JsonNode fetchOverview() {
        URI uri = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/v4/overview")
                .queryParam("client_locale", clientLocale)
                .build()
                .toUri();
        return fetchJson(uri, "overview");
    }

    private JsonNode fetchJson(URI uri, String label) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(45))
                .header("User-Agent", userAgent)
                .header("Accept", "application/json")
                .GET()
                .build();

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return objectMapper.readTree(response.body());
                }
                log.error("Lidl leaflet {} request failed with status {} (attempt {}/{})",
                        label, response.statusCode(), attempt, MAX_ATTEMPTS);
            } catch (IOException e) {
                log.error("Lidl leaflet {} request failed (attempt {}/{}): {}",
                        label, attempt, MAX_ATTEMPTS, e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Lidl leaflet {} request interrupted.", label);
                return null;
            }
        }
        return null;
    }

    private LidlLeafletDescriptor toDescriptor(JsonNode flyerNode,
                                               String category,
                                               String subcategory,
                                               String status) {
        String identifier = extractFlyerIdentifier(flyerNode);
        return LidlLeafletDescriptor.builder()
                .flyerId(text(flyerNode, "id"))
                .flyerIdentifier(identifier)
                .name(text(flyerNode, "name"))
                .title(text(flyerNode, "title"))
                .category(category)
                .subcategory(subcategory)
                .status(status)
                .active(flyerNode.path("isActive").asBoolean(false))
                .flyerJsonUrl(text(flyerNode, "flyerJson"))
                .build();
    }

    private String extractFlyerIdentifier(JsonNode flyerNode) {
        String flyerJson = text(flyerNode, "flyerJson");
        if (StringUtils.hasText(flyerJson) && flyerJson.contains("flyer_identifier=")) {
            int start = flyerJson.indexOf("flyer_identifier=") + "flyer_identifier=".length();
            int end = flyerJson.indexOf('&', start);
            return end > start ? flyerJson.substring(start, end) : flyerJson.substring(start);
        }
        String absoluteUrl = text(flyerNode, "flyerUrlAbsolute");
        if (absoluteUrl.contains("/prospekte/")) {
            return absoluteUrl.split("/prospekte/")[1].split("/")[0];
        }
        return text(flyerNode, "id");
    }

    private boolean isIncludedCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return false;
        }
        return includedCategories.contains(category.toLowerCase());
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("").trim();
    }

    private String trimTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
