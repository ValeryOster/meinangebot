package de.angebot.main.gathering.lidl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extrahiert Produktdaten aus Lidl-Kampagnenseiten.
 * Die Seite enthält pro Produkt ein {@code data-selector="PRODUCT"}-Element
 * mit Preis-/Rabatt-JSON im vorhergehenden HTML-Block.
 */
@Slf4j
@Component
public class LidlHtmlParser {

    private static final Pattern PRODUCT_SELECTOR = Pattern.compile("data-selector=\"PRODUCT\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRODUCT_ID = Pattern.compile("productid=\"(\\d+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRICE = Pattern.compile("&quot;price&quot;:\\{&quot;price&quot;:([\\d.]+)");
    private static final Pattern DELETED_PRICE = Pattern.compile("&quot;deletedPrice&quot;:([\\d.]+)");
    private static final Pattern OLD_PRICE = Pattern.compile("&quot;oldPrice&quot;:([\\d.]+)");
    private static final Pattern DISCOUNT = Pattern.compile("&quot;percentageDiscount&quot;:(\\d+)");
    private static final Pattern BRAND = Pattern.compile(
            "&quot;brand&quot;:\\{&quot;url&quot;:.*?&quot;name&quot;:&quot;([^&]+?)&quot;,&quot;showBrand&quot;:true");
    private static final Pattern CATEGORY = Pattern.compile("&quot;wonCategoryPrimary&quot;:&quot;([^&]+?)&quot;");
    private static final Pattern DESCRIPTION = Pattern.compile(
            "&quot;description&quot;:&quot;(.{0,2000}?)&quot;,&quot;moreDetails&quot;");
    private static final Pattern EAN = Pattern.compile("&quot;ians&quot;:\\[&quot;(\\d+?)&quot;");
    private static final Pattern STORE_BADGE = Pattern.compile(
            "&quot;text&quot;:&quot;(nur in der Filiale[^&]+?)&quot;,&quot;type&quot;:&quot;IN_STORE");

    private final ObjectMapper objectMapper;

    public LidlHtmlParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<LidlRawOffer> parseCampaignPage(String html, String campaignUrl) {
        if (!StringUtils.hasText(html)) {
            log.warn("Lidl campaign page is empty: {}", campaignUrl);
            return List.of();
        }

        Map<String, LidlRawOffer> offersByProductId = new LinkedHashMap<>();
        Matcher selectorMatcher = PRODUCT_SELECTOR.matcher(html);

        while (selectorMatcher.find()) {
            int start = Math.max(0, selectorMatcher.start() - 6000);
            int end = Math.min(html.length(), selectorMatcher.end() + 1500);
            String chunk = html.substring(start, end);

            Matcher productIdMatcher = PRODUCT_ID.matcher(chunk);
            if (!productIdMatcher.find()) {
                continue;
            }
            String productId = productIdMatcher.group(1);
            if (offersByProductId.containsKey(productId)) {
                continue;
            }

            Matcher priceMatcher = PRICE.matcher(chunk);
            if (!priceMatcher.find()) {
                continue;
            }

            try {
                LidlRawOffer offer = buildOffer(chunk, productId, priceMatcher.group(1), campaignUrl);
                offersByProductId.put(productId, offer);
            } catch (RuntimeException e) {
                log.error("Lidl product {} on {} could not be parsed: {}", productId, campaignUrl, e.getMessage());
            }
        }

        log.info("Lidl parser extracted {} offers from {}", offersByProductId.size(), campaignUrl);
        return new ArrayList<>(offersByProductId.values());
    }

    private LidlRawOffer buildOffer(String chunk, String productId, String priceText, String campaignUrl) {
        BigDecimal currentPrice = parsePrice(priceText);
        BigDecimal originalPrice = firstPrice(DELETED_PRICE, chunk);
        if (originalPrice == null) {
            originalPrice = firstPrice(OLD_PRICE, chunk);
        }
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            originalPrice = null;
        }

        Integer discountPercent = firstInt(DISCOUNT, chunk);
        if (discountPercent == null && originalPrice != null && currentPrice != null
                && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
            discountPercent = originalPrice.subtract(currentPrice)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(originalPrice, 0, RoundingMode.HALF_UP)
                    .intValue();
        }

        String listName = attribute(chunk, "listname");
        String title = attribute(chunk, "fulltitle");
        if (!StringUtils.hasText(title)) {
            title = "Produkt " + productId;
        }

        long startEpoch = parseLongAttribute(chunk, "storestartdate");
        long endEpoch = parseLongAttribute(chunk, "storeenddate");

        String description = firstMatch(DESCRIPTION, chunk);
        if (StringUtils.hasText(description)) {
            description = HtmlUtils.htmlUnescape(stripHtml(description));
        }

        String category = firstMatch(CATEGORY, chunk);
        if (!StringUtils.hasText(category)) {
            category = listName;
        }

        String storeBranch = firstMatch(STORE_BADGE, chunk);
        if (!StringUtils.hasText(storeBranch)) {
            storeBranch = "Filialangebot";
        }

        return LidlRawOffer.builder()
                .productId(productId)
                .title(title)
                .description(description)
                .currentPrice(currentPrice)
                .originalPrice(originalPrice)
                .discountPercent(discountPercent != null ? discountPercent : 0)
                .brand(firstMatch(BRAND, chunk))
                .category(category)
                .actionWeek(listName)
                .imageUrl(attribute(chunk, "image"))
                .canonicalPath(attribute(chunk, "canonicalurl"))
                .ean(firstMatch(EAN, chunk))
                .validFrom(epochToDate(startEpoch))
                .validTo(epochToDate(endEpoch))
                .storeBranch(storeBranch)
                .campaignUrl(campaignUrl)
                .offerSource(LidlOfferSource.CAMPAIGN)
                .rawJson(buildRawJson(productId, title, currentPrice, originalPrice, chunk))
                .build();
    }

    private String buildRawJson(String productId, String title, BigDecimal price, BigDecimal oldPrice, String chunk) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("productId", productId);
        node.put("title", title);
        if (price != null) {
            node.put("price", price);
        }
        if (oldPrice != null) {
            node.put("originalPrice", oldPrice);
        }
        node.put("sourceChunkLength", chunk.length());
        return node.toString();
    }

    private BigDecimal parsePrice(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return new BigDecimal(value.trim());
    }

    private BigDecimal firstPrice(Pattern pattern, String chunk) {
        Matcher matcher = pattern.matcher(chunk);
        if (matcher.find()) {
            return parsePrice(matcher.group(1));
        }
        return null;
    }

    private Integer firstInt(Pattern pattern, String chunk) {
        Matcher matcher = pattern.matcher(chunk);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private String firstMatch(Pattern pattern, String chunk) {
        Matcher matcher = pattern.matcher(chunk);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String attribute(String chunk, String name) {
        Pattern pattern = Pattern.compile(name + "=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(chunk);
        return matcher.find() ? matcher.group(1) : "";
    }

    private long parseLongAttribute(String chunk, String name) {
        String value = attribute(chunk, name);
        if (!StringUtils.hasText(value)) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private LocalDate epochToDate(long epochSeconds) {
        if (epochSeconds <= 0) {
            return null;
        }
        return Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.of("Europe/Berlin")).toLocalDate();
    }

    private String stripHtml(String html) {
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }
}
