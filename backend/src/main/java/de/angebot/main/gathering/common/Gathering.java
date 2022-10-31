package de.angebot.main.gathering.common;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

@Slf4j
public abstract class Gathering implements ErrorHandler {
    public abstract void startGathering() throws RuntimeException;

    public abstract String getDiscountName();

    public Document getDocument(String url) {
        Document document = new Document(url);
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("!!! Url ist nicht erreichbar");
            errorMessage.send(e.getMessage());
        }
        return document;
    }
}
