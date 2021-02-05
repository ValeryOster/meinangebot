package de.angebot.main.gathering.lidl;

import de.angebot.main.enities.Lidl;
import de.angebot.main.enities.ProductMaker;
import de.angebot.main.gathering.common.ErrorHandler;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.repositories.LidlRepo;
import de.angebot.main.repositories.ProductMakerRepo;
import de.angebot.main.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LidlOffer implements Gathering, ErrorHandler {

    private String mainUrl = "https://www.lidl.de/de/";

    private List<String> possibleMakers;

    @Autowired
    private LidlRepo lidlRepo;

    @Autowired
    private ProductMakerRepo productMakerRepo;

    @Override
    public void startGathering() {
        List<Lidl> kategorieList = getLidlKategorieList("filial-angebote");
        kategorieList.addAll(getLidlKategorieList("angebote"));
        kategorieList.forEach(lidl -> {
            Document doc = getDocument(lidl.getUrl());
            if (lidl.getUrl().contains("frische")) {
                saveFrischeProducte(lidl, doc);
            } else {
                saveOtherItems(lidl, doc);
            }
        });
    }

    private void saveOtherItems(Lidl lidl, Document doc) {
        possibleMakers = productMakerRepo.findAll()
                .stream()
                .filter(productMaker -> productMaker.getValid())
                .map(ProductMaker::getMakerName)
                .collect(Collectors.toList());
        List<String> allItemsUrl = getAllItemsUrl(doc);
        allItemsUrl.forEach(itemUrl -> {
            Document document = getDocument(itemUrl);

            Lidl newItem = copyNewLidl(lidl);
            getItemName(document, newItem);
            String description = getItemDescription(document);
            String prise = getItemPrise(document);
            String oldPrise = getItemOldPrise(document);
            getImageUrl(document, newItem);

            newItem.setProduktDescription(description);
            newItem.setProduktPrise(prise);
            newItem.setProduktRegularPrise(oldPrise);
            newItem.setUrl(itemUrl);

            try {
                lidlRepo.save(newItem);
                System.out.println(newItem.getProduktName() + " save");
            } catch (DataIntegrityViolationException e) {
                log.error(newItem.getUrl());
            }
        });

    }

    @NotNull
    private Lidl copyNewLidl(Lidl lidl) {
        Lidl newLidl = new Lidl();
        newLidl.setBisDate(lidl.getBisDate());
        newLidl.setDiscounterName(lidl.getDiscounterName());
        newLidl.setKategorie(lidl.getKategorie());
        newLidl.setImageLink(lidl.getImageLink());
        newLidl.setProduktMaker(lidl.getProduktMaker());
        newLidl.setProduktName(lidl.getProduktName());
        newLidl.setProduktRegularPrise(lidl.getProduktRegularPrise());
        newLidl.setProduktPrise(lidl.getProduktPrise());
        newLidl.setProduktDescription(lidl.getProduktDescription());
        newLidl.setUrl(lidl.getUrl());
        newLidl.setVonDate(lidl.getVonDate());

        return newLidl;
    }

    private void getImageUrl(Document document, Lidl lidl) {
        Elements select = document.getElementsByClass("carousel-product-detail");
        if (!select.isEmpty()) {
            String href = "https://www.lidl.de" + select.select("a")
                    .first()
                    .attr("href");
            lidl.setImageLink(Utils.downloadImage(href, "lidl", Utils.getNextSaturday()));
        } else {
            log.error("Image is not founded " + lidl.getUrl());
        }
    }

    private String getItemOldPrise(Document document) {
        Element pricelabel = document.getElementsByClass("pricelabel__price")
                .first()
                .getElementById("oldPriceId");
        if (pricelabel != null) {
            return pricelabel.text();
        }
        return "null";
    }

    private String getItemPrise(Document document) {
        Elements pricelabel = document.getElementsByClass("pricelabel__price-middleline");
        if (pricelabel.size() > 0) {
            String text = pricelabel.first()
                    .text()
                    .replace(" ", "")
                    .replace("-.", "0.")
                    .replace("*", "");
            return text;
        } else {
            log.error("!!! Preise werden nicht erkannt. !!!");
        }
        return "null";
    }

    private String getItemDescription(Document document) {
        Elements itemDesc = document.getElementsByClass("product-detail-container");
        if (itemDesc.size() > 0) {
            return itemDesc.first()
                    .text();
        }
        return "";
    }

    private void getItemName(Document document, Lidl lidl) {
        Elements elementsByClass = document.getElementsByClass("product-detail-hero");
        String descriptionName = elementsByClass.first()
                .getElementsByTag("h1")
                .text();
        String maker = getMakerFromString(descriptionName);
        lidl.setProduktName(descriptionName);
        lidl.setProduktMaker(maker);
    }

    private String getMakerFromString(String descriptionName) {
        List<String> collect = possibleMakers.stream()
                .filter(s -> descriptionName.toUpperCase()
                        .contains(s.toUpperCase()))
                .limit(1)
                .collect(Collectors.toList());
        if (collect.size() > 0) {
            return collect.get(0);
        }
        return "";
    }

    private List<String> getAllItemsUrl(Document doc) {
        List<String> itemsUrl = new ArrayList<>();
        Elements elementsByClass = doc.select("li.product-grid__item>a.product-tile");
        if (elementsByClass.size() != 0) {
            elementsByClass.forEach(element -> {
                String url = mainUrl + element.attr("href");
                itemsUrl.add(url);
            });
        } else {
            log.error("!!! Lidl angeboten werden nicht ausgelesen !!!");
        }
        return itemsUrl;
    }

    private void saveFrischeProducte(Lidl lidl, Document doc) {
        Elements itemsTop = doc.getElementsByClass("product-frische-tag");
        itemsTop.forEach(element -> {
            Lidl newItem = copyNewLidl(lidl);
            String imageUrl = getImage(element);
            String price = getPreise(element);
            String priceOld = getOldPreise(element);
            String itemName = getItemName(element);
            setDateOtherDateIfExists(newItem, element);

            newItem.setProduktName(itemName);
            newItem.setImageLink(imageUrl);
            newItem.setProduktPrise(price);
            newItem.setProduktRegularPrise(priceOld);

            lidlRepo.save(newItem);
        });
    }

    private String getItemName(Element element) {
        return element.getElementsByClass("description")
                .first()
                .getElementsByTag("h1")
                .first()
                .text()
                .replace(",", "")
                .replace("lose", "");
    }

    private void setDateOtherDateIfExists(Lidl lidl, Element element) {
        Elements elementsByClass = element.getElementsByClass("small-hint");
        if (!elementsByClass.isEmpty()) {
            String text = elementsByClass.first()
                    .text();
            if (!text.isEmpty()) {
                lidl.setBisDate(getDatum(text));
            }
        }
    }

    private String getPreise(Element element) {
        return element.getElementsByClass("red-price-tag")
                .first()
                .ownText()
                .replace("-.", "0.");
    }

    private String getOldPreise(Element element) {
        Elements elementsByClass = element.getElementsByClass("old-price");
        if (elementsByClass.size() > 0) {
            return elementsByClass.first()
                    .ownText()
                    .replace("-.", "0.");
        } else {
            return "null";
        }
    }

    //get Image localpath
    private String getImage(Element element) {
        String img = element.select("img")
                .first()
                .attr("src");
        return Utils.downloadImage("https://www.lidl.de" + img, "lidl", Utils.getNextSaturday());
    }

    private List<Lidl> getLidlKategorieList(String url) {
        Document document = getDocument(mainUrl + url);
        List<Lidl> kategorieList = new ArrayList<>();
        getKategorie(kategorieList, document);
        return kategorieList;
    }

    private void getKategorie(List<Lidl> lidlKategorie, Document document) {
        Elements elementsByClass = document.getElementsByClass("offerteaser__itemlink");
        for (Element el : elementsByClass) {
            Lidl lidl = new Lidl();
            String link = el.attr("href");
            if (!link.contains("https")) {
                String offer = (mainUrl + link);
                if (!offer.contains("test") && !offer.contains("de//de")) {
                    String kategorie = el.getElementsByClass("offerteaser__headline")
                            .first()
                            .text();
                    String dateFromHtml = el.getElementsByClass("offerteaser__subheadline")
                            .first()
                            .text();
                    lidl.setVonDate(getDatum(dateFromHtml));
                    lidl.setBisDate(Utils.getNextSaturday());
                    lidl.setUrl(offer);
                    lidl.setKategorie(kategorie);

                    lidlKategorie.add(lidl);
                }
            }
        }
    }


    public LocalDate getDatum(String dateText) {
        String datum = dateText.replaceAll("(.*)(\\d{2}.\\d{2}.)", "$2");
        if (!datum.isEmpty() && Character.isDigit(datum.charAt(0))) {
            LocalDate parse = LocalDate.parse((datum + (LocalDate.now()
                            .getYear())),
                    DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            return parse;
        } else {
            if (LocalDate.now()
                    .getDayOfWeek() == DayOfWeek.SUNDAY) {
                return Utils.getNextSaturday();
            } else {
                return Utils.getLastMonday();
            }
        }
    }

    @Override
    public String getDiscountName() {
        return "LIDL";
    }

    private Document getDocument(String url) {
        Document document = null;
        try {
            document = Jsoup.connect(url)
                    .get();
        } catch (IOException e) {
            log.error("!!! Lidl-Url ist nicht erreichbar");
            errorMessage.send(e.getMessage());
        }
        return document;
    }
}
