package de.angebot.main.gathering.edeka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.angebot.main.enities.ProductMaker;
import de.angebot.main.enities.discounters.Edeka;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.repositories.discounters.EdekaRepo;
import de.angebot.main.repositories.services.ProductMakerRepo;
import de.angebot.main.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component("edeka")
public class EdekaOffer extends Gathering {
    private static final String CHROME_BINARY = "C:\\Users\\oster\\Desktop\\chrome-win64\\chrome.exe";
    private final String mainUrl = "https://www.edeka.de/api/offers?limit=999&marketId=8002195";
    private final String OFFER_URL = "https://www.edeka.de/eh/rhein-ruhr/edeka-frischecenter-burkowski-altendorfer-stra%C3%9Fe-230/angebote.jsp";

    @Autowired
    private ProductMakerRepo productMakerRepo;

    @Autowired
    private EdekaRepo edekaRepo;


    @Override
    public void startGathering() {
        JsonNode jsonNode = getDocumentWithSelenium();
        ArrayList<JsonNode> offers = Lists.newArrayList(jsonNode.get("offers").elements());
        LocalDate dateFrom = getDate(jsonNode, true);
        for (JsonNode offer : offers) {
            Edeka edeka = new Edeka();
            setProductNameUndMaker(offer, edeka);
            edeka.setProduktPrise(offer.get("price").get("value").asText());
            edeka.setProduktDescription(getDescription(offer));
            edeka.setKategorie(offer.get("category").get("name").asText());
            edeka.setBisDate(getDate(offer, false));
            edeka.setVonDate(dateFrom);
            edeka.setImageLink(getImageLink(offer));
            edeka.setUrl(OFFER_URL);
            edekaRepo.save(edeka);
        }
    }

    private String getImageLink(JsonNode offer) {
        String imgUrl = offer.get("images").get("original").asText();
        if (!imgUrl.isEmpty()) {
            return Utils.downloadImage(imgUrl, "edeka", Utils.getNextSaturday(), "");
        }
        return null;
    }

    private static String getDescription(JsonNode offer) {
        Iterator<JsonNode> descriptions = offer.get("descriptions").elements();
        if (descriptions.hasNext()) {
            return descriptions.next().toString().replace("\"", "");
        }
        return "";
    }

    private static LocalDate getDate(JsonNode offer, boolean dateValidator) {
        if (dateValidator) {
            if (!offer.get("validFrom").asText().isEmpty()) {
                return LocalDate.parse(offer.get("validFrom").asText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        } else {
            if (!offer.get("validTill").asText().isEmpty()) {
                return LocalDate.parse(offer.get("validTill").asText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        }
        return null;
    }

    private void setProductNameUndMaker(JsonNode offer, Edeka edeka) {
        List<String> collect = productMakerRepo.findAll().stream().filter(ProductMaker::getValid)
                .map(ProductMaker::getMakerName).toList();
        String title = offer.get("title").asText();
        for (String maker : collect) {
            if (title.toLowerCase().contains(maker.toLowerCase())) {
                String replace = title.toLowerCase().replace(maker.toLowerCase(), "");
                String capitalize = StringUtils.capitalize(replace);
                edeka.setProduktName(capitalize);
                edeka.setProduktMaker(maker);
            }
        }
    }

    public JsonNode getDocumentWithSelenium() {
        Document parse = null;
        System.setProperty("webdriver.chrome.driver", seleniumDriverPath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments(firstArg);
        if (activeProfile.equals("prod")) {
            options.addArguments(secondArg, thirdArg);
        }else {
            options.setBinary(CHROME_BINARY);
        }
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get(mainUrl);
            driver.manage().window().maximize();
            String pre = driver.findElement(By.tagName("pre")).getText();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(pre);
            Thread.sleep(3000);
            return jsonNode;
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(driver.getPageSource());
            return null;
        } finally {
            driver.close();
        }
    }

    @Override
    public String getDiscountName() {
        return "Edeka";
    }
}
