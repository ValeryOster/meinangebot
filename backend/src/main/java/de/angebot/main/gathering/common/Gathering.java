package de.angebot.main.gathering.common;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public abstract class Gathering implements ErrorHandler {
    @Value("${selenium.path}")
    protected String seleniumDriverPath;

    @Value("${first.arg}")
    protected String firstArg;

    @Value("${second.arg}")
    protected String secondArg;

    @Value("${third.arg}")
    protected String thirdArg;

    @Value("${spring.profiles.active}")
    protected String activeProfile;

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

    protected void scrollPageDownWithJS(WebDriver driver, int interval) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        //Scroll down till the bottom of the page
        for (int i = 0; i < interval; i += 100) {
            js.executeScript("window.scrollBy(0," + i + ")");
            Thread.sleep(100);
        }
    }
}
