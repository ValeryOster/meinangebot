package de.angebot.main.gathering.globus;

import de.angebot.main.gathering.common.ErrorHandler;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class GlobusOffer extends Gathering implements ErrorHandler {

    String mainUrl = "https://www.globus.de/produkte/angebote-der-woche/";
    @Override
    public void startGathering() {

        Document document = getDocument(mainUrl);
//        Elements elementsByClass = document.getElementsByClass("product--box box--basic");
//        for (Element el : elementsByClass) {
            Elements roots = document.getElementsByClass("product--subtitle");
            for (Element e : roots) {
                log.info(e.text());
            }
//        }
    }

    @Override
    public String getDiscountName() {
        return null;
    }

    public Document getDocument(String url) {
        Document document = null;
        try {
            Map<String, String> cookies = new HashMap<>();
            cookies.put("Cookie", "marktFirm=1001; marktName=St.%20Wendel; marktAbb=WND; defaultStore=0");
            document = Jsoup.connect(url).cookies(cookies).get();
            Utils.saveHtmlToDisk(document, "C:/Users/oster/Desktop/ProjektAngebote/Dokument.html");
        } catch (IOException e) {
            log.error("!!! Url ist nicht erreichbar");
            errorMessage.send(e.getMessage());
        }
        return document;
    }

    public GlobusOffer() {
    }
}
