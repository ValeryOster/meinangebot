package de.angebot.main.gathering.netto;

import de.angebot.main.enities.Netto;
import de.angebot.main.gathering.common.ErrorHandler;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.repositories.NettoRepo;
import de.angebot.main.utils.SaveUtil;
import de.angebot.main.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class NettoOffer implements Gathering, ErrorHandler {
    @Autowired
    SaveUtil saveUtil;

    private String mainUrl = "https://www.netto-online.de/filialangebote";

    @Autowired
    private NettoRepo nettoRepo;

    @Override
    public void startGathering() {
        log.info("******** Netto parsing is starting. ********");
        Document document = getDocument(mainUrl);
        Map<String, List<Element>> katerogieMap = getKategorieMap(document);
        Utils.saveHtmlToDisk(document);
        if (document != null) {
            Elements elementsByClass = document.getElementsByClass("product-list__overflow-wrapper");
            if (elementsByClass.size() > 0) {
                for (Element element : elementsByClass) {
                    Netto netto = new Netto();
                    netto.setDiscounterName(getDiscountName());
                    netto.setProduktName(getProduktNameFromDocument(element));
                    //netto.setKategorie();
                    netto.setImageLink(getImage(element));
                    netto.setBisDate(Utils.getNextSaturday());
                }
            }
        }
        log.info("******** Netto parsing is ended. ********");
    }

    private Map<String, List<Element>> getKategorieMap(Document document) {
        Elements elementsByClass = document.getElementsByClass("sub-navigation__inner__list");
        for (Element element : elementsByClass) {
            Elements mainKategori = element.select("sub-navigation__list__item__link__parent headline__smallest");
            if (mainKategori.size() > 0) {

                String s = mainKategori.first().ownText();
                System.out.println(s);
            }
        }

        return null;
    }

    private String getProduktNameFromDocument(Element document) {
        Elements productName = document.getElementsByClass("product__title");
        if (productName.size() <= 0) {
            return null;
        }
        return productName.first().ownText();
    }

    private String getImage(Element element) {
        Elements imagesElements = element.getElementsByClass("image-transition");
        if (imagesElements.size() <= 0) {
            return null;
        }
        Element first = imagesElements.first();
        String src = first.absUrl("src");
        return Utils.downloadImageNextSaturdayWithOutName(src, getDiscountName());
    }

    @Override
    public String getDiscountName() {
        return "Netto";
    }

    private Document getDocument(String url) {
        Document document = null;
        try {
            document = Jsoup.connect(url).cookie("netto_user_stores_id", "1156").method(Connection.Method.GET).execute()
                    .parse();
        } catch (IOException e) {
            log.error("!!! Url ist nicht erreichbar --> " + url);
            errorMessage.send(e.getMessage());
            return null;
        }
        return document;
    }
}
