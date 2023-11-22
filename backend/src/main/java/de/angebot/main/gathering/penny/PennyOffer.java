package de.angebot.main.gathering.penny;


import de.angebot.main.enities.discounters.Penny;
import de.angebot.main.gathering.common.GatheringWithSelenium;
import de.angebot.main.repositories.discounters.PennyRepo;
import de.angebot.main.utils.SaveUtil;
import de.angebot.main.utils.Utils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Setter
@Component("penny")
@Configuration
public class PennyOffer extends GatheringWithSelenium {

    @Value("#{${map.of.penny.days}}")
    Map<Integer, List<String>> mapOfDaysElement;
    @Autowired
    private SaveUtil saveUtil;
    @Value("${penny.mainUrl}")
    private String mainUrl;
    @Autowired
    private PennyRepo pennyRepo;



    @Override
    public void startGathering() {
        Document document = getDocumentWithSelenium(mainUrl + "/angebote");
        if (document != null) {
            getWeekParts(document).forEach(this::saveOffers);
        }
    }

    private void saveOffers(LocalDate startDate, Element weekdayOffer) {
        List<String> angebotLinks = getOffersLinks(weekdayOffer);
        if (angebotLinks != null) {
            angebotLinks.forEach(angebot -> {
                Penny penny = new Penny();
                penny.setVonDate(startDate);
                LocalDate endDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
                penny.setBisDate(endDate);

                String url = mainUrl + angebot;
                Document offer = getDocument(url);
                if (offer != null) {
                    String price = offer.select("div.bubble__wrap-inner>span").text();
                    String origPrice = offer
                            .select("div.bubble.bubble__price--yellow.detail-block__price-bubble > div > " + "div > div " + ">" + " div > span")
                            .text().replaceAll("[a-zA-Z]", "").trim();

                    //          If star, facke offer, go to next
                    if (price.isEmpty() || price.contains("*")) {
                        return;
                    }

                    String offerName = offer.select("h1.detail-block__hdln").first().html().replace("*", "");
                    List<String> strings = Utils.splittToNameOrMaker(offerName);

                    //Look for a image link
                    penny.setImageLink(Utils.downloadImage(getImageLink(offer), "penny", endDate, ""));
                    penny.setProduktMaker(saveItemMaker(strings.get(0)));
                    penny.setProduktName(strings.get(1));
                    penny.setProduktPrise(price);
                    penny.setProduktRegularPrise(origPrice);
                    penny.setKategorie(getCategory(offer));
                    penny.setUrl(url);

                    pennyRepo.save(penny);
                }
            });
        }
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
                    int category1 = wholeData.indexOf("category");
                    if (category1 > 0) {
                        String category = wholeData.substring(category1);
                        kategorie = category.substring(0, category.indexOf(',')).split(":")[1].replace("\"", "");
                    }
                }
            }
        }
        return textPrettyPrint(kategorie);
    }

    private String textPrettyPrint(String kategorie) {
        List<String> konnektor = Stream.of("und", "fuer", "seit").toList();
        StringBuilder prettyKategorie = new StringBuilder();
        String[] split = kategorie.replace("-", " ").split(" ");
        for (String word : split) {
            if (!konnektor.contains(word)) {
                prettyKategorie.append(" ").append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
            } else {
                prettyKategorie.append(" ").append(word);
            }
        }
        return prettyKategorie.toString().trim();
    }

    private String getImageLink(Document offer) {
        try {
            Element elementById = offer.getElementById("offer-image-slide-0");
            if (elementById != null) {
                return elementById.select("img").first().attr("src");
            } else {
                Elements elementById1 = offer.getElementsByClass("detail-block__carousel-slide");
                if (!elementById1.isEmpty()) {
                    return elementById1.first().select("img").first().attr("src");
                }
            }

        } catch (NullPointerException e) {
            log.error(errorMessage.toString());
        }
        return "";
    }

    //to get Date, neen to know date of week
    private Map<LocalDate, Element> getWeekParts(Document offer) {
        Map<LocalDate, Element> weekArray = new HashMap<>();
        mapOfDaysElement.forEach((Integer dayOfWeek, List<String> elementTags) -> {
            for (String tag : elementTags) {
                Element elementById = offer.getElementById(tag);
                if (elementById != null) {
                    weekArray.put(Utils.getDate(dayOfWeek), elementById);
                    return;
                } else {
                    log.error("!!! ID: " + tag + " gibt es nicht !!!");
                }
            }
        });

        return weekArray;
    }



    private List<String> getOffersLinks(Element document) {
        Elements elementsByClass = document.getElementsByClass("tile__link--cover");
        if (!elementsByClass.isEmpty()) {
            return elementsByClass.stream().map(element -> element.attr("href")).filter(s -> s.contains("/angebote/")).toList();
        } else {
            log.error("Es wurde keine Angebote gefunden");
            return Collections.emptyList();
        }
    }



    public Document getDocumentFromChromeDriver(WebDriver driver) {
        By id = By.tagName("button");
        List<WebElement> elements = driver.findElements(id);
        for (WebElement element : elements) {
            if (element.getText().equals("Erlauben")) {
                element.click();
                break;
            }
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        //Scroll down till the bottom of the page
        for (int i = 0; i < 3000; i += 100) {
            js.executeScript("window.scrollBy(0," + i + ")");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return Jsoup.parse(driver.getPageSource());
    }

    @Override
    public String getDiscountName() {
        return "Penny";
    }
}

