package de.angebot.main.gathering.aldi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.angebot.main.enities.discounters.Aldi;
import de.angebot.main.enities.offers.Offer;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.repositories.discounters.AldiRepo;
import de.angebot.main.repositories.offers.OfferRepository;
import de.angebot.main.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component("aldi")
public class AldiOffer extends Gathering {

    private static final String RETAILER = "aldi-nord";
    private static final String MAIN_URL = "https://www.aldi-nord.de";
    private static final String OFFER_URL = MAIN_URL + "/angebote.html";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0 Safari/537.36";
    private static final int MAX_ATTEMPTS = 3;
    private static final Pattern NEXT_DATA_PATTERN = Pattern.compile(
            "<script[^>]*id=[\"']__NEXT_DATA__[\"'][^>]*>(.*?)</script>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
            "<script[^>]*>(.*?)</script>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AldiRepo aldiRepo;
    private final OfferRepository offerRepository;

    public AldiOffer(ObjectMapper objectMapper, AldiRepo aldiRepo, OfferRepository offerRepository) {
        this.objectMapper = objectMapper;
        this.aldiRepo = aldiRepo;
        this.offerRepository = offerRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public void startGathering() {
        String html = fetchHtml();
        List<JsonNode> jsonNodes = extractJson(html);
        List<RawAldiOffer> rawOffers = parseOffers(jsonNodes);
        int inserted = 0;
        int skipped = 0;

        for (RawAldiOffer rawOffer : rawOffers) {
            try {
                Optional<Offer> offer = normalizeOffer(rawOffer);
                if (offer.isEmpty()) {
                    skipped++;
                    continue;
                }
                if (saveOffer(offer.get())) {
                    inserted++;
                } else {
                    skipped++;
                }
            } catch (RuntimeException e) {
                skipped++;
                log.error("Aldi offer could not be normalized: {}", e.getMessage());
            }
        }

        log.info("Aldi offers parsed: {}, inserted: {}, skipped: {}", rawOffers.size(), inserted, skipped);
    }

    public String fetchHtml() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(OFFER_URL))
                .timeout(Duration.ofSeconds(25))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "de-DE,de;q=0.9,en;q=0.7")
                .GET()
                .build();

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return response.body();
                }
                log.error("Aldi HTML request failed with status {} (attempt {}/{})",
                        response.statusCode(), attempt, MAX_ATTEMPTS);
            } catch (IOException e) {
                log.error("Aldi HTML request failed (attempt {}/{}): {}", attempt, MAX_ATTEMPTS, e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Aldi HTML request was interrupted.");
                return "";
            }
        }
        return "";
    }

    public List<JsonNode> extractJson(String html) {
        if (!StringUtils.hasText(html)) {
            log.error("Aldi HTML is empty. No JSON can be extracted.");
            return List.of();
        }

        Map<String, Boolean> candidates = new LinkedHashMap<>();
        Matcher nextDataMatcher = NEXT_DATA_PATTERN.matcher(html);
        while (nextDataMatcher.find()) {
            candidates.put(nextDataMatcher.group(1).trim(), true);
        }

        Matcher scriptMatcher = SCRIPT_PATTERN.matcher(html);
        while (scriptMatcher.find()) {
            String script = scriptMatcher.group(1).trim();
            if (looksLikeJson(script)) {
                candidates.putIfAbsent(script, false);
            } else if (script.contains("{") && script.contains("}")) {
                extractJsonObjects(script).forEach(candidate -> candidates.putIfAbsent(candidate, false));
            }
        }

        List<JsonNode> nodes = new ArrayList<>();
        for (Map.Entry<String, Boolean> candidate : candidates.entrySet()) {
            parseJson(candidate.getKey(), candidate.getValue()).ifPresent(nodes::add);
        }

        if (nodes.isEmpty()) {
            log.error("No valid embedded JSON found in Aldi HTML.");
        }
        return nodes;
    }

    public List<RawAldiOffer> parseOffers(List<JsonNode> jsonNodes) {
        for (JsonNode node : jsonNodes) {
            Optional<JsonNode> offerResponse = findOfferResponse(node);
            if (offerResponse.isPresent()) {
                return parseOfferResponse(offerResponse.get());
            }
        }
        log.error("No Aldi OFFER_GET response with product data was found.");
        return List.of();
    }

    public Optional<Offer> normalizeOffer(RawAldiOffer rawOffer) {
        JsonNode product = rawOffer.product();
        String title = firstText(product, "name", "title", "productName");
        if (!StringUtils.hasText(title)) {
            return Optional.empty();
        }

        JsonNode priceNode = firstObject(product, "currentPrice", "price");
        BigDecimal currentPrice = priceValue(priceNode, "priceValue", "value", "current");
        BigDecimal originalPrice = priceValue(priceNode.path("strikePrice"), "strikePriceValue", "value");
        if (originalPrice == null) {
            originalPrice = priceValue(product, "originalPrice", "regularPrice");
        }

        LocalDate validFrom = firstDate(rawOffer.startDate(), product, priceNode, "validFromLocalDate", "validFrom");
        LocalDate validTo = firstDate(rawOffer.endDate(), product, priceNode, "validUntilLocalDate", "validUntil", "validTo");
        String imageUrl = readPrimaryImage(product);
        String sourceUrl = buildSourceUrl(product);
        String externalId = generateId(firstText(product, "objectID", "id", "productId"), title,
                currentPrice, imageUrl, validFrom);

        Offer offer = new Offer();
        offer.setExternalId(externalId);
        offer.setRetailer(RETAILER);
        offer.setTitle(title);
        offer.setDescription(firstText(product, "shortDescription", "longDescription", "description"));
        offer.setCurrentPrice(currentPrice);
        offer.setOriginalPrice(originalPrice);
        offer.setCurrency("EUR");
        offer.setImageUrl(imageUrl);
        offer.setValidFrom(validFrom);
        offer.setValidTo(validTo);
        offer.setCategory(rawOffer.category());
        offer.setSourceUrl(sourceUrl);
        return Optional.of(offer);
    }

    public String generateId(String stableId, String title, BigDecimal price, String imageUrl, LocalDate validFrom) {
        if (StringUtils.hasText(stableId)) {
            return RETAILER + "-" + stableId.trim() + "-" + nullSafe(validFrom);
        }
        String hashSource = nullSafe(title) + "|" + nullSafe(price) + "|" + nullSafe(imageUrl) + "|" + nullSafe(validFrom);
        return RETAILER + "-" + sha1(hashSource);
    }

    public boolean saveOffer(Offer offer) {
        boolean saved = false;
        if (existsInDb(offer.getExternalId())) {
            log.info("Aldi offer already exists and will be skipped: {}", offer.getExternalId());
        } else {
            offerRepository.save(offer);
            saved = true;
        }
        return saveLegacyAldiOffer(offer) || saved;
    }

    public boolean existsInDb(String externalId) {
        return StringUtils.hasText(externalId) && offerRepository.existsById(externalId);
    }

    private List<RawAldiOffer> parseOfferResponse(JsonNode response) {
        JsonNode productMap = response.path("algoliaDataMap");
        JsonNode categories = response.path("categories");
        if (!productMap.isObject() || !categories.isArray()) {
            return List.of();
        }

        Map<String, RawAldiOffer> offersById = new LinkedHashMap<>();
        for (JsonNode period : categories) {
            LocalDate startDate = parseDate(text(period, "startDate"));
            LocalDate endDate = parseDate(text(period, "endDate"));
            for (JsonNode content : period.path("content")) {
                String category = text(content, "title");
                JsonNode productIds = content.path("productIds");
                if (!productIds.isArray()) {
                    continue;
                }
                for (JsonNode productIdNode : productIds) {
                    String productId = productIdNode.asText("");
                    JsonNode product = productMap.path(productId);
                    if (product.isMissingNode() || product.isNull()) {
                        log.error("Aldi product id '{}' was referenced but not found in algoliaDataMap.", productId);
                        continue;
                    }
                    offersById.putIfAbsent(productId + "|" + category + "|" + nullSafe(startDate),
                            new RawAldiOffer(productId, category, startDate, endDate, product));
                }
            }
        }
        return new ArrayList<>(offersById.values());
    }

    private boolean saveLegacyAldiOffer(Offer offer) {
        String maker = "";
        Optional<Aldi> existingAldi = aldiRepo.findByUrlAndVonDateAndKategorie(
                offer.getSourceUrl(), offer.getValidFrom(), offer.getCategory());
        if (existingAldi.isPresent()) {
            return updateMissingLegacyImage(existingAldi.get(), offer);
        }

        Aldi aldi = new Aldi();
        aldi.setProduktName(offer.getTitle());
        aldi.setProduktMaker(maker);
        aldi.setProduktPrise(toPriceString(offer.getCurrentPrice()));
        aldi.setProduktRegularPrise(toPriceString(offer.getOriginalPrice()));
        aldi.setProduktDescription(offer.getDescription());
        aldi.setImageLink(downloadLegacyImage(offer));
        aldi.setVonDate(offer.getValidFrom());
        aldi.setBisDate(offer.getValidTo());
        aldi.setKategorie(offer.getCategory());
        aldi.setUrl(offer.getSourceUrl());
        aldiRepo.save(aldi);
        return true;
    }

    private boolean updateMissingLegacyImage(Aldi aldi, Offer offer) {
        if (StringUtils.hasText(aldi.getImageLink()) && !aldi.getImageLink().startsWith("http")) {
            return false;
        }
        String imageLink = downloadLegacyImage(offer);
        if (!StringUtils.hasText(imageLink)) {
            return false;
        }
        aldi.setImageLink(imageLink);
        aldiRepo.save(aldi);
        return true;
    }

    private String downloadLegacyImage(Offer offer) {
        LocalDate endDate = offer.getValidTo() != null ? offer.getValidTo() : offer.getValidFrom();
        return Utils.downloadImage(offer.getImageUrl(), "aldi", endDate, "");
    }

    private String toPriceString(BigDecimal price) {
        return price == null ? "" : price.toPlainString();
    }

    private Optional<JsonNode> findOfferResponse(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return Optional.empty();
        }

        JsonNode apiData = node.at("/props/pageProps/apiData");
        if (apiData.isTextual()) {
            Optional<JsonNode> apiDataNode = parseJson(apiData.asText(), true);
            if (apiDataNode.isPresent()) {
                return findOfferResponse(apiDataNode.get());
            }
        }

        if (isOfferResponse(node)) {
            return Optional.of(node);
        }

        if (node.isArray()) {
            if (node.size() >= 2 && "OFFER_GET".equals(node.get(0).asText(""))) {
                JsonNode response = node.get(1).path("res");
                if (isOfferResponse(response)) {
                    return Optional.of(response);
                }
            }
            for (JsonNode child : node) {
                Optional<JsonNode> found = findOfferResponse(child);
                if (found.isPresent()) {
                    return found;
                }
            }
        } else if (node.isObject()) {
            for (JsonNode child : node) {
                Optional<JsonNode> found = findOfferResponse(child);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }

    private boolean isOfferResponse(JsonNode node) {
        return node != null
                && node.path("algoliaDataMap").isObject()
                && node.path("categories").isArray();
    }

    private boolean looksLikeJson(String value) {
        return StringUtils.hasText(value) && (value.startsWith("{") || value.startsWith("["));
    }

    private Optional<JsonNode> parseJson(String value, boolean logErrors) {
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readTree(value));
        } catch (JsonProcessingException e) {
            if (logErrors) {
                log.error("Aldi embedded JSON could not be parsed: {}", e.getOriginalMessage());
            }
            return Optional.empty();
        }
    }

    private List<String> extractJsonObjects(String script) {
        List<String> objects = new ArrayList<>();
        for (int start = script.indexOf('{'); start >= 0; start = script.indexOf('{', start + 1)) {
            int depth = 0;
            boolean inString = false;
            boolean escaped = false;
            for (int index = start; index < script.length(); index++) {
                char current = script.charAt(index);
                if (escaped) {
                    escaped = false;
                    continue;
                }
                if (current == '\\') {
                    escaped = true;
                    continue;
                }
                if (current == '"') {
                    inString = !inString;
                    continue;
                }
                if (inString) {
                    continue;
                }
                if (current == '{') {
                    depth++;
                } else if (current == '}') {
                    depth--;
                    if (depth == 0) {
                        objects.add(script.substring(start, index + 1));
                        break;
                    }
                }
            }
        }
        return objects;
    }

    private JsonNode firstObject(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.path(field);
            if (value.isObject()) {
                return value;
            }
        }
        return objectMapper.createObjectNode();
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = text(node, field);
            if (StringUtils.hasText(value) && !"null".equalsIgnoreCase(value)) {
                return value;
            }
        }
        return "";
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return "";
        }
        return value.asText("").trim();
    }

    private BigDecimal priceValue(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.path(field);
            if (value.isNumber()) {
                return value.decimalValue();
            }
            if (value.isTextual() && StringUtils.hasText(value.asText())) {
                try {
                    return new BigDecimal(value.asText().replace(",", ".").replaceAll("[^0-9.]", ""));
                } catch (NumberFormatException e) {
                    log.error("Aldi price '{}' could not be parsed.", value.asText());
                }
            }
        }
        return null;
    }

    private LocalDate firstDate(LocalDate fallback, JsonNode product, JsonNode price, String... fields) {
        for (String field : fields) {
            LocalDate date = parseDate(text(price, field));
            if (date != null) {
                return date;
            }
            date = parseDate(text(product, field));
            if (date != null) {
                return date;
            }
        }
        return fallback;
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            if (value.matches("\\d+")) {
                return Instant.ofEpochSecond(Long.parseLong(value))
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }
            return LocalDate.parse(value);
        } catch (DateTimeParseException | NumberFormatException e) {
            log.error("Aldi date '{}' could not be parsed.", value);
            return null;
        }
    }

    private String readPrimaryImage(JsonNode product) {
        for (JsonNode asset : product.path("assets")) {
            if ("primary".equalsIgnoreCase(text(asset, "type"))) {
                return text(asset, "url");
            }
        }
        return "";
    }

    private String buildSourceUrl(JsonNode product) {
        String slug = text(product, "productSlug");
        if (!StringUtils.hasText(slug)) {
            return OFFER_URL;
        }
        return MAIN_URL + "/produkt/" + slug + ".html";
    }

    private String sha1(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is not available.", e);
        }
    }

    private String nullSafe(Object value) {
        return Objects.toString(value, "");
    }

    @Override
    public String getDiscountName() {
        return "Aldi-Nord";
    }

    private record RawAldiOffer(String productId, String category, LocalDate startDate, LocalDate endDate, JsonNode product) {
    }
}
