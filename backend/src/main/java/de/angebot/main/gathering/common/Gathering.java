package de.angebot.main.gathering.common;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public abstract class Gathering implements ErrorHandler {

    @Autowired
    private Environment environment;

    @Value("${selenium.chrome.driver-path:}")
    protected String seleniumDriverPath;

    @Value("#{'${selenium.chrome.arguments:}'.split(',')}")
    protected List<String> chromeArguments;

    protected ChromeOptions createChromeOptions() {
        if (StringUtils.hasText(seleniumDriverPath)) {
            System.setProperty("webdriver.chrome.driver", seleniumDriverPath);
        }
        ChromeOptions options = new ChromeOptions();
        chromeArguments.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .forEach(options::addArguments);
        return options;
    }

    protected boolean isProdProfile() {
        return environment.acceptsProfiles(Profiles.of("prod"));
    }

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
