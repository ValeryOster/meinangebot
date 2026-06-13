package de.angebot.main.gathering.lidl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Parst Prospekt-Produkte aus der Leaflets-API-Antwort ({@code flyer.products}).
 */
@Slf4j
@Component
public class LidlLeafletParser {

    private final ObjectMapper objectMapper;

    public LidlLeafletParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<LidlLeafletOffer> parseFlyer(JsonNode flyerResponse, LidlLeafletDescriptor descriptor) {
        if (flyerResponse == null || !flyerResponse.path("success").asBoolean(false)) {
            log.warn("Lidl leaflet response invalid for {}", descriptor.getFlyerIdentifier());
            return List.of();
        }

        JsonNode flyer = flyerResponse.path("flyer");
        if (flyer.isMissingNode()) {
            return List.of();
        }

        LocalDate validFrom = parseDate(text(flyer, "offerStartDate"));
        LocalDate validTo = parseDate(text(flyer, "offerEndDate"));
        String actionWeek = descriptor.getName() + " (" + descriptor.getTitle() + ")";
        Map<String, Integer> linkIdToPage = buildLinkPageIndex(flyer.path("pages"));

        List<LidlLeafletOffer> offers = new ArrayList<>();
        JsonNode products = flyer.path("products");
        if (!products.isObject()) {
            log.warn("Lidl leaflet {} has no products object.", descriptor.getFlyerIdentifier());
            return List.of();
        }

        Iterator<Map.Entry<String, JsonNode>> fields = products.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            try {
                LidlLeafletOffer offer = toOffer(entry.getKey(), entry.getValue(), descriptor, flyer,
                        validFrom, validTo, actionWeek, linkIdToPage);
                if (offer != null) {
                    offers.add(offer);
                }
            } catch (RuntimeException e) {
                log.error("Lidl leaflet product {} in {} could not be parsed: {}",
                        entry.getKey(), descriptor.getFlyerIdentifier(), e.getMessage());
            }
        }

        log.info("Lidl leaflet parser extracted {} products from {} ({})",
                offers.size(), descriptor.getFlyerIdentifier(), descriptor.getTitle());
        return offers;
    }

    public LidlRawOffer toRawOffer(LidlLeafletOffer leafletOffer) {
        return LidlRawOffer.builder()
                .productId(leafletOffer.getProductId())
                .title(leafletOffer.getTitle())
                .description(leafletOffer.getDescription())
                .currentPrice(leafletOffer.getCurrentPrice())
                .originalPrice(null)
                .discountPercent(0)
                .brand(leafletOffer.getBrand())
                .category(leafletOffer.getCategory())
                .actionWeek(leafletOffer.getActionWeek())
                .imageUrl(leafletOffer.getImageUrl())
                .canonicalPath(leafletOffer.getCanonicalPath())
                .ean(null)
                .validFrom(leafletOffer.getValidFrom())
                .validTo(leafletOffer.getValidTo())
                .storeBranch("Filialangebot (Prospekt)")
                .campaignUrl(leafletOffer.getSourceUrl())
                .offerSource(LidlOfferSource.LEAFLET)
                .leafletId(leafletOffer.getLeafletId())
                .leafletPage(leafletOffer.getLeafletPage())
                .rawJson(leafletOffer.getRawJson())
                .build();
    }

    private LidlLeafletOffer toOffer(String linkId,
                                     JsonNode productNode,
                                     LidlLeafletDescriptor descriptor,
                                     JsonNode flyer,
                                     LocalDate validFrom,
                                     LocalDate validTo,
                                     String actionWeek,
                                     Map<String, Integer> linkIdToPage) {
        String productId = text(productNode, "productId");
        String title = text(productNode, "title");
        if (!StringUtils.hasText(productId) || !StringUtils.hasText(title)) {
            return null;
        }

        BigDecimal price = parsePrice(text(productNode, "price"));
        if (price == null) {
            return null;
        }

        String description = text(productNode, "description");
        if (StringUtils.hasText(description)) {
            description = HtmlUtils.htmlUnescape(stripHtml(description));
        }

        String category = text(productNode, "wonCategoryPrimary");
        if (!StringUtils.hasText(category)) {
            category = descriptor.getCategory();
        }

        return LidlLeafletOffer.builder()
                .productId(productId)
                .title(title)
                .description(description)
                .currentPrice(price)
                .brand(text(productNode, "brand"))
                .category(category)
                .imageUrl(text(productNode, "image"))
                .canonicalPath(text(productNode, "canonicalUrl"))
                .sourceUrl(firstNonBlank(text(productNode, "url"), buildSourceUrl(productNode)))
                .validFrom(validFrom)
                .validTo(validTo)
                .actionWeek(actionWeek)
                .leafletPage(linkIdToPage.get(linkId))
                .leafletId(text(flyer, "id"))
                .flyerIdentifier(descriptor.getFlyerIdentifier())
                .flyerName(descriptor.getName())
                .flyerTitle(descriptor.getTitle())
                .flyerCategory(descriptor.getCategory())
                .rawJson(productNode.toString())
                .build();
    }

    private Map<String, Integer> buildLinkPageIndex(JsonNode pages) {
        Map<String, Integer> linkIdToPage = new HashMap<>();
        if (!pages.isArray()) {
            return linkIdToPage;
        }
        for (JsonNode page : pages) {
            int pageNumber = page.path("number").asInt(0);
            for (JsonNode link : page.path("links")) {
                if ("product".equalsIgnoreCase(text(link, "displayType"))) {
                    linkIdToPage.put(text(link, "id"), pageNumber);
                }
            }
        }
        return linkIdToPage;
    }

    private String buildSourceUrl(JsonNode productNode) {
        String canonical = text(productNode, "canonicalUrl");
        if (!StringUtils.hasText(canonical)) {
            return "";
        }
        return "https://www.lidl.de" + (canonical.startsWith("/") ? canonical : "/" + canonical);
    }

    private BigDecimal parsePrice(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.replace(",", ".").trim());
        } catch (NumberFormatException e) {
            log.error("Lidl leaflet price '{}' could not be parsed.", value);
            return null;
        }
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            log.error("Lidl leaflet date '{}' could not be parsed.", value);
            return null;
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("").trim();
    }

    private String firstNonBlank(String first, String second) {
        return StringUtils.hasText(first) ? first : second;
    }

    private String stripHtml(String html) {
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }
}
