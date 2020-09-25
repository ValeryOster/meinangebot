package de.angebot.main.gathering.penny;

import de.angebot.main.enities.Penny;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.repositories.PennyRepo;
import de.angebot.main.utils.Utils;
import lombok.Setter;
import lombok.extern.java.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log
@Setter
@Component
@ConfigurationProperties(prefix = "penny")
public class PennyOffer implements Gathering {
    @Value("${penny.montag}")
    private String montag;

    @Value("${penny.donnerstag}")
    private String donnerstag;

    @Value("${penny.freitag}")
    private String freitag;

    @Value("${penny.mainUrl}")
    private String mainUrl;

    @Autowired
    private PennyRepo pennyRepo;

    @Override
    public void startGathering() {
        Document document = getDocument(mainUrl + "/angebote");
        if (document != null) {
            getWeekParts(document).forEach(this::saveOffers);
        }
    }

    private void saveOffers(LocalDate startDate, Element weekdayOffer) {

        List<String> angebotLinks = getOffersLinks(weekdayOffer);
        angebotLinks.forEach(angebot -> {
            Penny penny = new Penny();
            penny.setVonDate(startDate);
            LocalDate endDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
            penny.setBisDate(endDate);

            Document offer = getDocument(mainUrl + angebot);
            String price = offer.select("div.bubble__wrap-inner>span").text();
//            String origPrice = offer.select("div.bubble__small-value>span").text();
            String origPrice = offer.select("div.bubble.bubble__price--yellow.detail-block__price-bubble > div > div > div > div > span")
                    .text()
                    .replaceAll("[a-zA-Z]", "").trim();
//            If star, facke offer, go to next
            if (price.isEmpty() || price.contains("*")) {
                return;
            }

            String offerName = offer.select("h1.detail-block__hdln")
                    .first()
                    .html()
                    .replace("*", "");
            System.out.println(offerName + ", "+ price+" , orig = " +(origPrice.isEmpty() ? "0":origPrice));
            List<String> strings = Utils.splittToNameOrMaker(offerName);
            String imagLink = offer.select("div.detail-block__carousel-slide>img")
                    .attr("src");
            if (imagLink == null || imagLink.isEmpty()) {
                Elements select = offer.select("#offer-image-slide-0>img");
                imagLink = select
                        .attr("src");
            }
            penny.setImageLink(Utils.downloadImage(imagLink,"penny", endDate));
            penny.setProduktMaker(strings.get(0));
            penny.setProduktName(strings.get(1));
            penny.setProduktPrise(price);
            penny.setProduktRegularPrise(origPrice);
//            System.out.println("Hersteller: " + penny.getProduktMaker() + ", Name:" + penny.getProduktName() + " - "
//                    + penny.getProduktPrise() + " /nBild: " + penny.getImageLink());
            
            
            pennyRepo.save(penny);
            System.out.println("Gespeichert: " + penny.getProduktName());
        });
    }

    //to get Date, neen to know date of week
    private Map<LocalDate, Element> getWeekParts(Document offer) {
        Map<LocalDate, Element> weekArray = new HashMap<>();
        weekArray.put(Utils.getDate(2), offer.getElementById(montag));
        weekArray.put(Utils.getDate(5), offer.getElementById(donnerstag));
        weekArray.put(Utils.getDate(6), offer.getElementById(freitag));
        return weekArray;
    }


    private List<String> getOffersLinks(Element document) {
        return document.getElementsByClass("tile__link--cover ellipsis")
                .stream()
                .map(element -> element.attr("href"))
                .filter(s -> s.contains("/angebote/"))
                .collect(Collectors.toList());
    }

    private Document getDocument(String url) {
        Document document = null;
        try {
            document = Jsoup.connect(url)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document;
    }


    @Override
    public String getDiscountName() {
        return "Penny";
    }
}

