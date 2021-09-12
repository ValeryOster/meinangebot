package de.angebot.main.gathering.netto;

import de.angebot.main.gathering.common.ErrorHandler;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.repositories.NettoRepo;
import de.angebot.main.utils.SaveUtil;
import de.angebot.main.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;


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
        Utils.saveHtmlToDisk(document);
        if (document != null) {
            Elements elementsByClass = document.getElementsByClass("product-list__overflow-wrapper");
            if (elementsByClass.size() > 0) {

            }
        }
        log.info("******** Netto parsing is ended. ********");
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
