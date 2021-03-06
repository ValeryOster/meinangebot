package de.angebot.main.gathering.penny;

import de.angebot.main.enities.Penny;
import de.angebot.main.gathering.common.ErrorHandler;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.repositories.PennyRepo;
import de.angebot.main.utils.SaveUtil;
import de.angebot.main.utils.Utils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Setter
@Component
@Configuration
public class PennyOffer implements Gathering, ErrorHandler {

    @Value("${penny.mainUrl}")
    private String mainUrl;

    @Value("#{${map.of.penny.days}}")
    Map<Integer, List<String>> mapOfDaysElement;

    @Autowired
    private PennyRepo pennyRepo;

    @Autowired
    SaveUtil saveUtil;

    @Override
    public void startGathering() {
        log.info("********Penny parsing is starting.********");
        Document document = getDocument(mainUrl + "/angebote");
        if (document != null) {
            getWeekParts(document).forEach(this::saveOffers);
        }
        log.info("********Penny parsing is ended.********");
    }

    private void saveOffers(LocalDate startDate, Element weekdayOffer) {
        List<String> angebotLinks = getOffersLinks(weekdayOffer);
        angebotLinks.forEach(angebot -> {
            Penny penny = new Penny();
            penny.setVonDate(startDate);
            LocalDate endDate = LocalDate.now()
                    .with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
            penny.setBisDate(endDate);

            String url = mainUrl + angebot;
            Document offer = getDocument(url);
            if (offer != null) {
                String price = offer.select("div.bubble__wrap-inner>span").text();
                String origPrice = offer.select("div.bubble.bubble__price--yellow.detail-block__price-bubble > div > " +
                        "div > div > div > span")
                        .text()
                        .replaceAll("[a-zA-Z]", "")
                        .trim();

//          If star, facke offer, go to next
                if (price.isEmpty() || price.contains("*")) {
                    return;
                }

                String offerName = offer.select("h1.detail-block__hdln")
                        .first()
                        .html()
                        .replace("*", "");
                List<String> strings = Utils.splittToNameOrMaker(offerName);

                //Look for a image link
                String imagLink = getImageLink(offer);
                penny.setImageLink(Utils.downloadImage(imagLink, "penny", endDate, ""));
                penny.setProduktMaker(saveItemMaker(strings.get(0)));
                penny.setProduktName(strings.get(1));
                penny.setProduktPrise(price);
                penny.setProduktRegularPrise(origPrice);
                penny.setKategorie(getCategory(offer));
                penny.setUrl(url);

                pennyRepo.save(penny);
                System.out.println("Gespeichert: " + penny.getKategorie());
            }
        });
    }

    private String saveItemMaker(String strings) {
        saveUtil.saveProduktMaker(strings);
        return strings;
    }

    private String getCategory(Element weekdayOffer) {
        Elements scriptElements = weekdayOffer.getElementsByTag("script");
        String kategorie = "";
        for (Element element : scriptElements) {
            for (DataNode node : element.dataNodes()) {
                String wholeData = node.getWholeData();
                if (wholeData.contains("window.pageData.products")) {
                    String category = wholeData.substring(wholeData.indexOf("category"));
                    kategorie = category.substring(0, category.indexOf(','))
                            .split(":")[1].replace("\"", "");
                }
            }
        }
        return textPrettyPrint(kategorie);
    }

    private String textPrettyPrint(String kategorie) {
        List<String> konnektor = Stream.of("und", "fuer", "seit")
                .collect(Collectors.toList());
        StringBuilder prettyKategorie = new StringBuilder();
        String[] split = kategorie.replaceAll("-", " ")
                .split(" ");
        for (String word : split) {
            if (!konnektor.contains(word)) {
                prettyKategorie.append(" " + word.substring(0, 1)
                        .toUpperCase())
                        .append(word.substring(1));
            } else {
                prettyKategorie.append(" " + word);
            }
        }
        return prettyKategorie.toString()
                .trim();
    }

    private String getImageLink(Document offer) {
        Element select = offer.select("div.detail-block__carousel-slide>noscript>img")
                .first();
        if (select == null) {
            select = offer.getElementById("offer-image-slide-0")
                    .select("img")
                    .first();
        }
        return select.attr("src");
    }

    //to get Date, neen to know date of week
    private Map<LocalDate, Element> getWeekParts(Document offer) {
        Map<LocalDate, Element> weekArray = new HashMap<>();
        mapOfDaysElement.forEach((Integer dayOfWeek, List<String> elementTags) ->{
            for (String tag : elementTags) {
                Element elementById = offer.getElementById(tag);
                if (elementById != null) {
                    weekArray.put(Utils.getDate(dayOfWeek), elementById);
                    return;
                }else {
                    log.error("!!! ID: " + tag + " gibt es nicht !!!");
                }
            }
        });

        return weekArray;
    }

    private List<String> getOffersLinks(Element document) {
        Elements elementsByClass = document.getElementsByClass("tile__link--cover ellipsis");
        if (!elementsByClass.isEmpty()) {
            return elementsByClass
                    .stream()
                    .map(element -> element.attr("href"))
                    .filter(s -> s.contains("/angebote/"))
                    .collect(Collectors.toList());
        }else {
            log.error("Es wurde keine Angebote gefunden");
            return null;
        }
    }

    private Document getDocument(String url) {
        Document document = null;
        try {
            document = Jsoup.connect(url)
                    .get();
        } catch (IOException e) {
            errorMessage.send(e.getMessage());
        }
        return document;
    }

    @Override
    public String getDiscountName() {
        return "Penny";
    }
}

