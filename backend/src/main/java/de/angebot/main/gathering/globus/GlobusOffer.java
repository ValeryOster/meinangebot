package de.angebot.main.gathering.globus;

import de.angebot.main.enities.ProductMaker;
import de.angebot.main.enities.discounters.Globus;
import de.angebot.main.gathering.common.ErrorHandler;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.repositories.discounters.GlobusRepo;
import de.angebot.main.repositories.services.ProductMakerRepo;
import de.angebot.main.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GlobusOffer extends Gathering implements ErrorHandler {

    @Autowired
    private GlobusRepo globusRepo;
    @Autowired
    private ProductMakerRepo productMakerRepo;

    String mainUrl = "https://www.globus.de/produkte/angebote-der-woche/";
    List<String> offerUrlList = new ArrayList<>();
    private List<String> possibleMakers;

    @Override
    public void startGathering() {
        initialize();
        getDocumentWithSelenium(mainUrl);
        saveURLsToDB(offerUrlList);
    }

    private void initialize() {
        offerUrlList.clear();
        possibleMakers = productMakerRepo.findAll().stream().filter(ProductMaker::getValid)
                .map(ProductMaker::getMakerName).collect(Collectors.toList());
    }


    private void saveURLsToDB(List<String> offerUrlList) {
        for (String url : offerUrlList) {
            Globus newOffer = new Globus();
            Document document = getDocument(url);
            if (document != null) {
                newOffer.setUrl(url);
                getProduktNameUndMakerName(document, newOffer);
                newOffer.setProduktPrise(getPrice(document));
                newOffer.setProduktRegularPrise(getRegularPrice(document));
                newOffer.setKategorie(getKategorie(document));
                newOffer.setImageLink(Utils.downloadImageNextSaturdayWithOutName(getImageLink(document), getDiscountName()));
                newOffer.setDiscounterName(getDiscountName());
                newOffer.setBisDate(Utils.getNextSaturday());
                newOffer.setVonDate(Utils.getNextMonday());

                globusRepo.save(newOffer);
            }
        }
    }

    private String getProduktDescription(Document document, String productDescription) {
        Elements elementsByClass = document.getElementsByClass("price--unit");
        if (!elementsByClass.isEmpty()) {
            return productDescription +", " + elementsByClass.get(0).text();
        }
        return null;

    }

    private String getImageLink(Document document) {
        Elements elementsByClass = document.getElementsByClass("image--media");
        if (!elementsByClass.isEmpty()) {
            return elementsByClass.get(0).getElementsByTag("img").attr("src");
        }
        return null;
    }

    private String getKategorie(Document document) {
        Elements elementsByClass = document.getElementsByClass("breadcrumb--list");
        if (!elementsByClass.isEmpty()) {
            return elementsByClass.get(0).getElementsByTag("a").get(2).text();
        }
        return "";
    }

    private String getProduktMaker(Document document) {
        return null;
    }

    private String getRegularPrice(Document document) {
        Elements elementsByClass = document.getElementsByClass("price--line-through");
        if (!elementsByClass.isEmpty()) {
            return elementsByClass.get(0).text().replaceAll(Utils.REGEX_WITHOUT_NUMBERS, "");
        }
        return null;

    }

    private String getPrice(Document document) {
        Elements elementsByClass = document.getElementsByClass("is--default-container");
        if (!elementsByClass.isEmpty()) {
            return elementsByClass.get(0).text().replaceAll(Utils.REGEX_WITHOUT_NUMBERS, "");
        }
        return null;
    }

    private void getProduktNameUndMakerName(Document document, Globus newOffer) {
        Elements elementsByClass = document.getElementsByClass("product--title");
        if (!elementsByClass.isEmpty()) {
            String[] split = elementsByClass.get(0).text().split(",");
            //save Product description
            newOffer.setProduktDescription(getProduktDescription(document, split.length > 1 ? split[1] : ""));

            String makerWithName = split[0];
            String[] splited = makerWithName.split(" ");
            if (splited.length > 1) {
                String makersNameFromList = Utils.getMakersNameFromList(possibleMakers, splited[0]);
                if (!makersNameFromList.isBlank()) {
                    newOffer.setProduktName(makersNameFromList);
                    newOffer.setProduktMaker(makerWithName.replace(makersNameFromList, "").trim());
                    return;
                }
            }
            newOffer.setProduktName(makerWithName);
        }
    }

    @Override
    public String getDiscountName() {
        return "Globus";
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

    public void getDocumentWithSelenium(String url) {
        Document parse = null;
        System.setProperty("webdriver.chrome.driver", seleniumDriverPath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments(firstArg);
        WebDriver driver = null;
        if (activeProfile.equals("prod")) {
            options.addArguments(secondArg, thirdArg);
        }
        if (activeProfile.equals("dev")) {
            options.setBinary("C:\\Users\\oster\\Desktop\\chrome-win64\\chrome.exe");
        }
        try {
            driver = new ChromeDriver(options);
            driver.get(url);
            driver.manage().window().maximize();
            Thread.sleep(2000);
            driver.findElement(new By.ById("CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll")).click();
            Thread.sleep(2000);
            chooseStore(driver);
            for (int i = 0; i < 30; i++) {
                scrollPageDownWithJS(driver, 1000);
                saveOfferToList(driver.getPageSource());
                WebElement element = driver.findElement(new By.ByCssSelector("ff-paging-item[type='nextLink']"));
                element.click();
                Thread.sleep(500);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            driver.close();
        }
    }

    private void chooseStore(WebDriver driver) throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            try {
                WebElement element = driver.findElement(new By.ByCssSelector("div[data-store-name='Essen']"));
                WebElement element1 = element.findElement(new By.ByCssSelector("a[class='button--green']"));
                element1.click();
                break;
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            Thread.sleep(500);
        }
        Thread.sleep(2000);
    }

    private void saveOfferToList(String pageSource) {
        Document document = Jsoup.parse(pageSource);
        Elements roots = document.getElementsByClass("product--box box--basic");
        for (Element element : roots) {
            element.getElementsByTag("a").forEach(a -> {
                String href = a.attr("href");
                if (!offerUrlList.contains(href)) {
                    offerUrlList.add(href);
                }
            });

        }
    }

    public GlobusOffer() {
    }
}
