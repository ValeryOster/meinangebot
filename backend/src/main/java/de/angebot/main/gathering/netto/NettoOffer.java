package de.angebot.main.gathering.netto;

import de.angebot.main.enities.ProductMaker;
import de.angebot.main.enities.discounters.Netto;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.repositories.discounters.NettoRepo;
import de.angebot.main.repositories.services.ProductMakerRepo;
import de.angebot.main.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverLogLevel;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Component("netto")
public class NettoOffer extends Gathering{

    @Value("${third.arg}")
    private String thirdArg;
    private List<String> possibleMakers;
    @Autowired
    private NettoRepo nettoRepo;
    @Autowired
    private ProductMakerRepo productMakerRepo;

    @Value("${selenium.path}")
    private String seleniumDriverPath;
    @Value("${first.arg}")
    private String firstArg;
    @Value("${second.arg}")
    private String secondArg;
    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Override
    public void startGathering() {
        String mainUrl = "https://www.netto-online.de/filialangebote";
        Document document = getDocumentWithSelenium(mainUrl);
        assert document != null;
        Map<String, String> katerogieMap = getKategorieMap(document);
        if (katerogieMap.size() > 0) {

            katerogieMap.forEach(this::saveOffers);
        }
    }

    private void saveOffers(String katerogieMap, String url) {
        Document document = getDocumentWithSelenium(url);
        if (document != null) {
            Elements elementsByClass = document.getElementsByClass("product-list__overflow-wrapper");
            if (elementsByClass.size() > 0) {
                for (Element element : elementsByClass) {
                    Netto netto = new Netto();
                    netto.setUrl(url);
                    netto.setDiscounterName(getDiscountName());
                    saveProduktNameAndMaker(netto, element);
                    netto.setKategorie(katerogieMap);
                    netto.setImageLink(getImage(element));
                    netto.setBisDate(Utils.getNextSaturday());
                    netto.setVonDate(getStartDay(document));
                    netto.setProduktPrise(getPrise(element));
                    netto.setProduktRegularPrise(getOldPrise(element));
                    netto.setProduktDescription(getDescription(element));
                    try {
                        nettoRepo.save(netto);
                    } catch (DataIntegrityViolationException e) {
                        log.error(netto.getUrl());
                    }
                }
            }
        }
    }

    private String getDescription(Element element) {
        Elements mainElement = element.select("span.product__info-wrapper");
        if (mainElement.size() > 0) {
            Elements discriptionElements = mainElement.first().select("span.product__base-price");
            if (discriptionElements.size() > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                discriptionElements.forEach(el -> {
                    String s = el.ownText();
                    if (!s.isEmpty()) {
                        stringBuilder.append(el.ownText()).append(", ");
                    }
                });
                return stringBuilder.toString();
            }
        }
        return null;
    }

    private String getPrise(Element element) {
        Elements select = element.select("ins.product__current-price");
        if (select.size() > 0) {
            String s = select.first().text();
            if (!s.isEmpty()) {
                String replace = s.replace("–", "0");
                return replace.replace("*", "");
            }
        }
        return "";
    }

    private String getOldPrise(Element element) {
        Elements select = element.select("del.product__old-price");
        if (select.size() > 0) {
            String s = select.first().text();
            if (!s.isEmpty()) {
                String replace = s.replace("-", "0").replace("statt", "");
                return replace.replace("*", "");
            }
        }
        return "";
    }

    private LocalDate getStartDay(Document document) {

        Elements select = document.select("h1.headline__major");
        if (select.size() > 0) {
            String str = select.first().ownText();
            return Utils.getDateFromString(str);
        }

        return Utils.getSuitableDay(DayOfWeek.MONDAY);
    }

    private Map<String, String> getKategorieMap(Document document) {
        Map<String, String> kategorienSammlung = new HashMap<>();
        Elements mainMenuElements = document
                .select("a.sub-navigation__list__item__link__parent:matchesOwn(Aktuelle Filial-Angebote)");
        if (mainMenuElements.size() > 0) {
            Elements underElements = mainMenuElements.first().nextElementSiblings();
            for (Element element : underElements) {
                if (element.getElementsContainingText("Angebote der Folgewoche").size() > 0) {
                    continue;
                }
                Elements mainKategori = element.select("li ul.sub-navigation__inner__list");
                if (mainKategori.size() > 0) {

                    Elements selected = mainKategori.first().select("a.sub-navigation__list__item__link__parent");
                    if (selected.size() > 0) {
                        for (Element pickOne : selected) {
                            putInMap(kategorienSammlung, pickOne);
                        }
                    }
                } else {
                    Element a = element.select("a").first();
                    putInMap(kategorienSammlung, a);
                }
            }
        }

        return kategorienSammlung;
    }

    private void putInMap(Map<String, String> kategorienSammlung, Element pickOne) {
        String name = pickOne.ownText();
        String href = pickOne.attr("href");
        kategorienSammlung.put(name, href);
    }

    private String getProduktNameFromDocument(Element document) {
        Elements productName = document.getElementsByClass("product__title");
        if (productName.size() <= 0) {
            return null;
        }
        return productName.first().ownText();
    }

    private void saveProduktNameAndMaker(Netto netto, Element document) {
        possibleMakers = productMakerRepo.findAll().stream().filter(ProductMaker::getValid)
                .map(ProductMaker::getMakerName).collect(Collectors.toList());
        String produktNameFromDocument = getProduktNameFromDocument(document);
        if (produktNameFromDocument != null) {
            String maker = getMakerFromString(produktNameFromDocument);
            String replace = Utils
                    .makeAllLetterCapitalize(produktNameFromDocument.toLowerCase().replace(maker.toLowerCase(), ""));
            String produktName = replace.trim();
            netto.setProduktName(produktName);
            netto.setProduktMaker(maker);
        }
    }

    private String getMakerFromString(String descriptionName) {
        Stream<String> stream = possibleMakers.stream();
        List<String> collect = stream.filter(s -> Utils.isContainExactWord(descriptionName.toUpperCase(), s)).limit(1)
                .collect(Collectors.toList());
        if (collect.size() > 0 && !collect.get(0).isEmpty()) {
            return StringUtils.capitalize(collect.get(0).toLowerCase());
        }
        return "";
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

    @Override
    public Document getDocument(String url) {
        Document document;
        try {
            document = Jsoup.connect(url)
                    .header("Content-Type","application/x-www-form-urlencoded")
                    .cookie("netto_user_stores_id", "9103")
                    .data("mode", "filterReviews")
                    .data("filterRating", "")
                    .data("filterSegment", "")
                    .data("filterSeasons", "")
                    .data("filterLang", "ALL")
                    .referrer("http://www.google.com")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36")
                    .method(Connection.Method.POST)
                    .execute()
                    .parse();
        } catch (IOException e) {
            log.error("!!! Url ist nicht erreichbar --> " + url);
            errorMessage.send(e.getMessage());
            return null;
        }
        return document;
    }
    public Document getDocumentWithSelenium(String url) {
        Document parse;
        System.setProperty("webdriver.chrome.driver", seleniumDriverPath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments(firstArg);

        try {
            options.setLogLevel(ChromeDriverLogLevel.ALL);
            WebDriver driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            driver.get(url);
            Thread.sleep(2000);

            driver.findElement(new By.ById("CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll")).click();

            Thread.sleep(2000);
            WebElement element = driver.findElement(new By.ByCssSelector(".btn-primary.js-layer-storefinder.hasLayerEvent"));
            element.click();

            Thread.sleep(10000);

        }catch (SessionNotCreatedException e) {
            log.error(e.getMessage());
            return null;
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        return null;
    }

}
