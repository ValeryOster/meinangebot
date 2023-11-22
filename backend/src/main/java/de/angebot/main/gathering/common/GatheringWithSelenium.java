package de.angebot.main.gathering.common;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Configuration
public abstract class GatheringWithSelenium extends Gathering {

    @Value("${selenium.path}")
    public String seleniumDriverPath;

    @Value("${first.arg}")
    public String firstArg;

    @Value("${second.arg}")
    public String secondArg;

    @Value("${third.arg}")
    public String thirdArg;

    @Value("${spring.profiles.active}")
    public String activeProfile;

    public Document getDocumentWithSelenium(String url) {
        Document parse = null;
        System.setProperty("webdriver.chrome.driver", seleniumDriverPath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments(firstArg);
        if (activeProfile.equals("prod")) {
            options.addArguments(secondArg, thirdArg);
        }
        try {
            WebDriver driver = new ChromeDriver(options);
            driver.get(url);
            driver.manage().window().maximize();
            try {
                Thread.sleep(3000);
                parse = getDocumentFromChromeDriver(driver);
            } catch (NoSuchElementException | InterruptedException e) {
                log.error(e.getMessage());
                return null;
            } finally {
                driver.close();
            }
        } catch (SessionNotCreatedException e) {
            log.error(e.getMessage());
            return null;
        }

        return parse;
    }

    protected abstract Document getDocumentFromChromeDriver(WebDriver driver);

}
