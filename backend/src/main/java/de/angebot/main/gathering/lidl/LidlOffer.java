package de.angebot.main.gathering.lidl;

import de.angebot.main.common.GermanyDayOfWeek;
import de.angebot.main.enities.discounters.Lidl;
import de.angebot.main.enities.ProductMaker;
import de.angebot.main.errors.SiteParsingError;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.repositories.discounters.LidlRepo;
import de.angebot.main.repositories.services.ProductMakerRepo;
import de.angebot.main.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class LidlOffer extends Gathering {
    private final String mainUrl = "https://www.lidl.de";
    private String angeboteUrl = "";
    private List<String> possibleMakers;
    @Autowired
    private LidlRepo lidlRepo;
    @Autowired
    private ProductMakerRepo productMakerRepo;

    @Override
    public void startGathering() {
        getAngeboteUrl();
        List<Lidl> allItemsUrl = new ArrayList<>();
        List<String> urlList = getURLs();
        urlList.forEach(s -> {
            Document document = getDocument(s);
            List<Lidl> allItemsUrl1 = getAllItemsUrl(document);
            allItemsUrl.addAll(allItemsUrl1);
        });

        allItemsUrl.forEach(lidl -> {
            Document doc = getDocument(lidl.getUrl());
            if (doc != null) {
                saveOtherItems(lidl, doc);
            }
        });
    }

    private void getAngeboteUrl() {
        String newMain = "https://www.lidl.de";
        Document document = getDocument(newMain);
        Elements elementsByAttributeValue = document.getElementsByAttributeValue("data-ga-label", "Filial-Angebote");
        if (elementsByAttributeValue.size() > 0) {
            angeboteUrl = newMain + elementsByAttributeValue.first().attr("href");
        }
    }

    private void saveOtherItems(Lidl lidl, Document doc) {
        possibleMakers = productMakerRepo.findAll().stream().filter(ProductMaker::getValid)
                .map(ProductMaker::getMakerName).collect(Collectors.toList());
        try {
            getItemName(doc, lidl);
            String description = getItemDescription(doc);
            String prise = getItemPrise(doc);
            String oldPrise = getItemOldPrise(doc);
            getImageUrl(doc, lidl);

            lidl.setProduktDescription(description);
            lidl.setProduktPrise(prise);
            lidl.setProduktRegularPrise(oldPrise);
            lidl.setBisDate(Utils.getNextSaturday());
            try {
                lidlRepo.save(lidl);
            } catch (DataIntegrityViolationException e) {
                log.error(lidl.getUrl());
            }
        } catch (SiteParsingError error) {
            log.error(error.getMessage());
        }
    }

    @NotNull
    private Lidl copyNewLidl(Lidl lidl) {
        Lidl newLidl = new Lidl();
        newLidl.setBisDate(lidl.getBisDate());
        newLidl.setDiscounterName(lidl.getDiscounterName());
        newLidl.setImageLink(lidl.getImageLink());
        newLidl.setKategorie(lidl.getKategorie());
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
        Elements select = document.select("img.gallery-image__img");
        if (select.size() > 0) {
            String href = select.first().attr("src");
            lidl.setImageLink(Utils.downloadImage(href, "lidl", Utils.getNextSaturday(), ""));
        } else {
            log.error("Image is not founded " + lidl.getUrl());
        }
    }

    private String getItemOldPrise(Document document) {
        Elements oldPreis = document.getElementsByClass("m-price__rrp");
        if (oldPreis.size() > 0) {
            return oldPreis.text();
        }
        return "null";
    }

    private String getItemPrise(Document document) {
        Elements pricelabel = document.getElementsByClass("m-price__price");
        if (pricelabel.size() > 0) {
            return pricelabel.first().text().replace(" ", "").replace("-.", "0.").replace("*", "");
        } else {
            throw new SiteParsingError("!!! Preise werden nicht erkannt. !!!");
        }
    }

    private String getItemDescription(Document document) {
        Elements itemDesc = document.select("div.keyfacts__description");
        if (itemDesc.size() > 0) {
            return itemDesc.first().text();
        }
        return "";
    }

    private void getItemName(Document document, Lidl lidl) {
        Elements select = document.select("h1.keyfacts__title");
        if (select != null) {
            Element first = select.first();
            String nameWithProducer = first.text();
            String maker = getMakerFromString(nameWithProducer);
            String target = "(?i)" + maker;
            String replace = nameWithProducer.replaceAll(target, "").trim();
            lidl.setProduktName(replace);
            lidl.setProduktMaker(maker);
        } else {
            throw new SiteParsingError("Name ist nicht gefunden worden");
        }
    }

    private String getMakerFromString(String descriptionName) {
        Stream<String> stream = possibleMakers.stream();
        List<String> collect = stream.filter(s -> descriptionName.toUpperCase().contains(s.toUpperCase())).limit(1)
                .collect(Collectors.toList());
        if (collect.size() > 0) {
            return collect.get(0);
        }
        return "";
    }

    private List<Lidl> getAllItemsUrl(Document doc) {
        List<Lidl> itemsUrl = new ArrayList<>();
        Elements elementsByClass = doc.getElementsByClass("ATheCampaign__SectionWrapper");
        if (elementsByClass.size() != 0) {
            elementsByClass.forEach(element -> {
                Lidl lidl = new Lidl();
                Elements aTheSectionHead = element.getElementsByClass("ATheSectionHead");
                if (aTheSectionHead.size() > 0) {
                    setKategorieName(lidl, aTheSectionHead);
                }
                setStartDayAndMainKategorie(doc, lidl);
                getItemURL(element, itemsUrl, lidl);
            });
        } else {
            log.error("!!! Lidl angeboten werden nicht ausgelesen !!!");
        }
        return itemsUrl;
    }

    private String getMainKategorie(Document doc) {
        String mainKategorieName = "";
        Elements mainElement = doc.getElementsByClass("ATheHeroStage__OfferAnchor--current");
        if (mainElement.size() > 0) {
            mainKategorieName = mainElement.first().text();
        }
        return mainKategorieName;
    }

    private void setKategorieName(Lidl lidl, Elements aTheSectionHead) {
        Element kategorieArea = aTheSectionHead.first();
        String kategorieName = kategorieArea.select("h2").first().text();
        lidl.setKategorie(kategorieName);
    }

    private void getItemURL(Element kategorieArea, List<Lidl> itemsUrl, Lidl lidl) {
        Elements aCampaignGrid__item = kategorieArea.getElementsByClass("ACampaignGrid__item");
        aCampaignGrid__item.forEach(element -> {
            Elements product = element.getElementsByAttributeValue("data-selector", "PRODUCT");
            if (product.size() > 0) {
                String first = product.first().attr("canonicalurl");
                Lidl newLidl = copyNewLidl(lidl);
                newLidl.setUrl(mainUrl + first);
                itemsUrl.add(newLidl);
            }
        });
    }

    private List<String> getURLs() {
        Document document = getDocument(angeboteUrl);

        Elements select = document.getElementsByClass("ATheHeroStage__TabPanels").first().select("section");

        return getAllURLs(select);
    }

    private List<String> getAllURLs(Elements select) {
        List<String> allURLs = new ArrayList<>();
        for (Element e : select) {
            Elements select1 = e.select("span:containsOwn(Lebensmittel)");
            if (select1.size() > 0) {
                saveURLs(allURLs, e);
            }
            select1 = e.select("span:containsOwn(Angebote diese Woche)");
            if (select1.size() > 0) {
                saveURLs(allURLs, e);
            }
        }
        return allURLs;
    }

    private void saveURLs(List<String> allURLs, Element mainElement) {
        Elements role = mainElement.getElementsByAttributeValue("role", "gridcell");
        for (Element childElement : role) {
            String href = childElement.select("a[href]").first().attr("href");
            if (checkItThisOnWeek(childElement)) {
                String testUrl = href.contains(mainUrl) ? "" : mainUrl;
                allURLs.add(testUrl + href);
            }
        }
    }

    private Boolean checkItThisOnWeek(Element e) {
        Elements arrayText = e.getElementsByClass("ATheHeroStage__OfferHeadlineText");
        if (arrayText.size() > 0) {
            String dateText = arrayText.first().text();
            if (dateText.contains("Testergebnisse")) {
                return false;
            }
            String datum = dateText.replaceAll("(.*)(\\d{2}.\\d{2}.)", "$2").replace(" sparen", "");
            if (!datum.isEmpty() && Character.isDigit(datum.charAt(0))) {
                try {
                    LocalDate parse = LocalDate.parse((datum + (LocalDate.now().getYear())), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    return !parse.isAfter(Utils.getNextSaturday());

                } catch (DateTimeParseException e1) {
                    log.error("LIDL -> Datum wird nicht erkannt -> Datum nach parsing: " + datum);
                }
            }
        }
        return true;
    }

    private void setStartDayAndMainKategorie(Document document, Lidl lidl) {
        String mainKategorie = getMainKategorie(document);
        for (GermanyDayOfWeek germanyDayOfWeek : GermanyDayOfWeek.values()) {
            String toString = germanyDayOfWeek.toString();
            if (mainKategorie.toLowerCase().contains(toString.toLowerCase())) {
                int ordinal = germanyDayOfWeek.getDayOfWeek() + 1;
                LocalDate date = Utils.getDate(ordinal);
                lidl.setVonDate(date);
            }
        }
        String kategorie = lidl.getKategorie();
        if (kategorie == null || kategorie.isEmpty()) {
            lidl.setKategorie(mainKategorie);
        }
    }

    @Override
    public String getDiscountName() {
        return "LIDL";
    }

}
