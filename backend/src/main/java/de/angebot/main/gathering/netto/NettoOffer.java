package de.angebot.main.gathering.netto;

import de.angebot.main.enities.ProductMaker;
import de.angebot.main.enities.discounters.Netto;
import de.angebot.main.gathering.common.BrowserHttpClient;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.gathering.common.GatheringException;
import de.angebot.main.repositories.discounters.NettoRepo;
import de.angebot.main.repositories.services.ProductMakerRepo;
import de.angebot.main.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sammelt die Filial-Angebote von netto-online.de.
 * <p>
 * Die Angebote sind nur sichtbar, wenn eine Filiale gewählt ist (Cookie {@code netto_user_stores_id}).
 * Die Kategorien stehen als Links {@code /filialangebote/{gruppe}/{kategorie}} in der Seitennavigation,
 * Gruppen ohne Unterkategorien (z. B. "Samstagskracher") als {@code /filialangebote/{gruppe}}.
 * Der Gültigkeitszeitraum steht in der Überschrift (h1), z. B.
 * "Obst &amp; Gemüse gültig von Montag, 23.11.26 - Samstag, 28.11.26".
 */
@Slf4j
@Component("netto")
public class NettoOffer extends Gathering {

    static final String STORE_COOKIE = "netto_user_stores_id";
    private static final Pattern CATEGORY_URL = Pattern.compile(".*/filialangebote/(\\d+)(?:/(\\d+))?/?$");
    private static final Pattern DATE = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{2})");
    private static final Pattern PRICE = Pattern.compile("(\\d+)[.,](\\d{2})");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yy");

    private final NettoRepo nettoRepo;
    private final ProductMakerRepo productMakerRepo;
    private final BrowserHttpClient httpClient;
    private final String mainUrl;
    private final String storeId;

    public NettoOffer(NettoRepo nettoRepo,
                      ProductMakerRepo productMakerRepo,
                      BrowserHttpClient httpClient,
                      @Value("${netto.mainUrl:https://www.netto-online.de/filialangebote}") String mainUrl,
                      @Value("${netto.storeId:9103}") String storeId) {
        this.nettoRepo = nettoRepo;
        this.productMakerRepo = productMakerRepo;
        this.httpClient = httpClient;
        this.mainUrl = mainUrl;
        this.storeId = storeId;
    }

    @Override
    public int startGathering() {
        Document mainPage = fetch(mainUrl);
        Map<String, String> categories = parseCategories(mainPage);
        if (categories.isEmpty()) {
            throw new GatheringException("Keine Netto-Kategorien gefunden (Filiale " + storeId
                    + " nicht gesetzt oder Seitenstruktur geändert).");
        }

        List<String> makers = loadKnownMakers();
        Set<String> seen = new HashSet<>();
        int saved = 0;
        int parsed = 0;
        int failedPages = 0;

        for (Map.Entry<String, String> category : categories.entrySet()) {
            Document page;
            try {
                page = fetch(category.getKey());
            } catch (GatheringException e) {
                failedPages++;
                log.error("Netto-Kategorie '{}' konnte nicht geladen werden: {}", category.getValue(), e.getMessage());
                continue;
            }
            List<Netto> offers = parseOffers(page, category.getValue(), category.getKey(), makers);
            parsed += offers.size();
            for (Netto netto : offers) {
                String key = netto.getProduktName() + "|" + netto.getProduktPrise() + "|" + netto.getVonDate();
                if (!seen.add(key) || exists(netto)) {
                    continue;
                }
                netto.setImageLink(downloadImage(netto));
                nettoRepo.save(netto);
                saved++;
            }
        }

        if (failedPages == categories.size()) {
            throw new GatheringException("Keine Netto-Kategorieseite konnte geladen werden (siehe Log).");
        }
        if (parsed == 0) {
            throw new GatheringException("Netto-Seiten geladen, aber keine Angebote gefunden – Seitenstruktur geändert?");
        }
        log.info("Netto: {} Kategorien, {} Angebote gelesen, {} neu gespeichert, {} Seiten fehlgeschlagen.",
                categories.size(), parsed, saved, failedPages);
        return saved;
    }

    /**
     * Liefert die zu ladenden Kategorien (URL → Name). Gruppen mit Unterkategorien werden durch ihre
     * Unterkategorien ersetzt, damit jedes Angebot seine genaue Kategorie erhält.
     */
    Map<String, String> parseCategories(Document document) {
        Map<String, String> groups = new LinkedHashMap<>();
        Map<String, String> subCategories = new LinkedHashMap<>();
        Set<String> groupsWithChildren = new HashSet<>();

        for (Element link : document.select("a.js-gtm-region-store-offer-category[href]")) {
            String url = link.absUrl("href");
            if (!StringUtils.hasText(url)) {
                url = link.attr("href");
            }
            Matcher matcher = CATEGORY_URL.matcher(url);
            String name = link.text().trim();
            if (!matcher.matches() || name.isEmpty()) {
                continue;
            }
            if (matcher.group(2) == null) {
                groups.putIfAbsent(matcher.group(1), url + "|" + name);
            } else {
                groupsWithChildren.add(matcher.group(1));
                subCategories.putIfAbsent(url, name);
            }
        }

        Map<String, String> result = new LinkedHashMap<>(subCategories);
        groups.forEach((groupId, urlAndName) -> {
            if (!groupsWithChildren.contains(groupId)) {
                int separator = urlAndName.indexOf('|');
                result.putIfAbsent(urlAndName.substring(0, separator), urlAndName.substring(separator + 1));
            }
        });
        return result;
    }

    /** Liest alle Angebote einer Kategorieseite. Bilder werden hier noch nicht geladen. */
    List<Netto> parseOffers(Document document, String category, String url, List<String> makers) {
        LocalDate[] validity = parseValidity(document);
        List<Netto> offers = new ArrayList<>();

        for (Element tile : document.select("#region_store_offers_list .product-list__item")) {
            String title = text(tile.selectFirst(".product__title"));
            String price = parsePrice(text(tile.selectFirst("ins.product__current-price")));
            if (title.isEmpty() || price.isEmpty()) {
                continue;
            }

            Netto netto = new Netto();
            String maker = findMaker(title, makers);
            String name = maker.isEmpty() ? title
                    : title.replaceFirst("(?i)" + Pattern.quote(maker), "").trim();
            netto.setProduktName(name.isEmpty() ? title : name);
            netto.setProduktMaker(maker);
            netto.setProduktPrise(price);
            netto.setProduktRegularPrise(parsePrice(text(tile.selectFirst("del.product__old-price"))));
            netto.setProduktDescription(parseDescription(tile));
            netto.setKategorie(category);
            netto.setUrl(url);
            netto.setVonDate(validity[0]);
            netto.setBisDate(validity[1]);

            Element image = tile.selectFirst("img.tc-product-image");
            netto.setImageLink(image == null ? "" : image.absUrl("src").isEmpty() ? image.attr("src") : image.absUrl("src"));
            offers.add(netto);
        }
        return offers;
    }

    LocalDate[] parseValidity(Document document) {
        String headline = text(document.selectFirst("h1.headline__major"));
        List<LocalDate> dates = new ArrayList<>();
        Matcher matcher = DATE.matcher(headline);
        while (matcher.find()) {
            try {
                dates.add(LocalDate.parse(matcher.group(1), DATE_FORMAT));
            } catch (RuntimeException e) {
                log.warn("Netto-Datum '{}' konnte nicht gelesen werden.", matcher.group(1));
            }
        }
        if (dates.isEmpty()) {
            return new LocalDate[]{Utils.getNextMonday(), Utils.getNextSaturday()};
        }
        return new LocalDate[]{dates.get(0), dates.get(dates.size() - 1)};
    }

    static String parsePrice(String raw) {
        Matcher matcher = PRICE.matcher(raw.replace(" ", "").replace("–", "0").replace("-", "0"));
        return matcher.find() ? matcher.group(1) + "." + matcher.group(2) : "";
    }

    private String parseDescription(Element tile) {
        List<String> parts = new ArrayList<>();
        for (Element property : tile.select(".product__properties .product-property")) {
            String value = property.text().trim();
            if (!value.isEmpty()) {
                parts.add(value);
            }
        }
        return String.join(", ", parts);
    }

    private String findMaker(String title, List<String> makers) {
        String upperTitle = title.toUpperCase();
        for (String maker : makers) {
            if (Utils.isContainExactWord(upperTitle, Pattern.quote(maker.toUpperCase()))) {
                return StringUtils.capitalize(maker.toLowerCase());
            }
        }
        return "";
    }

    private List<String> loadKnownMakers() {
        return productMakerRepo.findAll().stream()
                .filter(maker -> Boolean.TRUE.equals(maker.getValid()))
                .map(ProductMaker::getMakerName)
                .filter(StringUtils::hasText)
                .toList();
    }

    private boolean exists(Netto netto) {
        return nettoRepo.existsByProduktNameAndVonDateAndKategorie(
                netto.getProduktName(), netto.getVonDate(), netto.getKategorie());
    }

    private String downloadImage(Netto netto) {
        String url = netto.getImageLink();
        if (!StringUtils.hasText(url)) {
            return "";
        }
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        try {
            byte[] image = httpClient.getBytes(url, Map.of(STORE_COOKIE, storeId));
            return Utils.saveImage(image, "netto", netto.getBisDate(), fileName);
        } catch (GatheringException e) {
            log.warn("Netto-Bild nicht geladen: {}", e.getMessage());
            return "";
        }
    }

    Document fetch(String url) {
        String html = httpClient.getHtml(url, Map.of(STORE_COOKIE, storeId));
        return Jsoup.parse(html, url);
    }

    private static String text(Element element) {
        return element == null ? "" : element.text().trim();
    }

    @Override
    public String getDiscountName() {
        return "Netto";
    }
}
