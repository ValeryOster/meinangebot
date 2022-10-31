package de.angebot.main.gathering.aldi;

import de.angebot.main.enities.discounters.Aldi;
import de.angebot.main.gathering.common.ErrorHandler;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.repositories.discounters.AldiRepo;
import de.angebot.main.utils.SaveUtil;
import de.angebot.main.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class AldiOffer extends Gathering {

    @Autowired
    SaveUtil saveUtil;

    String mainUrl = "https://www.aldi-nord.de";
    private final String angebotUrl = mainUrl + "/angebote.html";


    @Autowired
    private AldiRepo aldiRepo;

    @Override
    public void startGathering() {
        Document document = getDocument(angebotUrl);
        getStartDay(document);
    }

    private void getStartDay(Document document) {
        Elements elementsByClass = document.getElementsByClass("mod-offers__day");
        for (Element el : elementsByClass) {
            Aldi aldi = new Aldi();

            aldi.setVonDate(getLocalDate(el));
            Elements categories = el.getElementsByClass("mod mod-tile-group");
            for (Element categorie : categories) {
                aldi.setKategorie(getKategorieName(categorie));
                List<String> itemMainUrl = getItemMainUrl(categorie);
                for (String itemUrl : itemMainUrl) {
                    Document itemDoc = getDocument(itemUrl);
                    if (itemDoc != null && itemDoc.select("div.mod.mod-article-intro.ct-non-food-artikel ") != null) {
                        Aldi newAldi = getNewAldi(aldi);
                        newAldi.setProduktName(getItemName(itemDoc));
                        newAldi.setUrl(itemUrl);
                        String imagePath = getImagePath(itemDoc, newAldi.getProduktName());
                        newAldi.setImageLink(imagePath);
                        newAldi.setImageMobileLink(resizeImage(imagePath));
                        newAldi.setProduktMaker(getItemMaker(itemDoc));
                        newAldi.setProduktPrise(getItemPrice(itemDoc));
                        newAldi.setProduktRegularPrise(getItemRegularPrice(itemDoc));
                        newAldi.setProduktDescription(getItemDescription(itemDoc));
                        saveEntity(newAldi);
                    }
                }
            }
        }
    }

    private String resizeImage(String imagePath) {

        try {
            String pathname = Utils.getImageDestinationFolder() + "" + imagePath;
            BufferedImage bufferedImage = ImageIO.read(new File(pathname));
            BufferedImage resize = Scalr.resize(bufferedImage, 200);
            Path path = Paths.get(imagePath);
            String imageName = path.getFileName().toString();
            Path root = path.getRoot();
            ImageIO.write(resize, "png", new File(root.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private List<String> getItemMainUrl(Element categorie) {
        List<String> urls = new ArrayList<>();
        Elements elementsByClass = categorie.select("div[data-tile-url]");
        for (Element element : elementsByClass) {
            //get intermediary
            String attr = mainUrl + element.attr("data-tile-url");
            Document document = getDocument(attr);

            //get itemLink
            Element select = document.getElementsByClass("mod-article-tile__action").first();
            if (select != null) {
                String href = select.attr("href");
                if (!href.contains("onlineshop")) {
                    urls.add(mainUrl + href);
                }
            }
        }
        return urls;
    }

    private Aldi getNewAldi(Aldi aldi) {
        Aldi aldiNew = new Aldi();
        aldiNew.setKategorie(aldi.getKategorie());
        aldiNew.setVonDate(aldi.getVonDate());
        aldiNew.setBisDate(Utils.getNextSaturday());
        return aldiNew;
    }

    private void saveEntity(Aldi aldi) {
        aldiRepo.save(aldi);
    }

    private String getItemDescription(Document itemDoc) {
        try {
            return itemDoc.getElementsByClass("rte").first().ownText();
        } catch (RuntimeException e) {
            log.error("!!! Aldi - Description ist nicht gefunden.");
            errorMessage.send(e.getMessage());
        }
        return null;
    }

    private String getItemRegularPrice(Document itemDoc) {
        try {
            Elements price__previous = itemDoc.getElementsByClass("price__previous");
            return price__previous.first().ownText();
        } catch (RuntimeException e) {
            log.error("!!! Aldi - ItemName ist nicht gefunden.");
            errorMessage.send(e.getMessage());
        }
        return null;
    }

    private String getItemPrice(Document itemDoc) {
        try {

            return itemDoc.getElementsByClass("price__wrapper").first().ownText();
        } catch (RuntimeException e) {
            log.error("!!! Aldi - ItemName ist nicht gefunden.");
            errorMessage.send(e.getMessage());
        }
        return null;
    }

    private String getItemMaker(Document itemDoc) {
        try {
            Element elementsByClass = itemDoc.getElementsByClass("mod-article-intro__header-headline-small").first();
            String maker = elementsByClass.ownText();
            if (!maker.isEmpty()) {
                saveUtil.saveProduktMaker(maker.toUpperCase());
                return maker;
            }
        } catch (RuntimeException e) {
            log.error("!!! Aldi - ItemMaker ist nicht gefunden.");
            errorMessage.send(e.getMessage());
        }

        return null;
    }

    private String getItemName(Document itemDoc) {
        try {
            Element elementsByClass = itemDoc.getElementsByClass("mod-article-intro__header-headline").select("h1")
                    .first();
            return elementsByClass.ownText();
        } catch (RuntimeException e) {
            log.error("!!! Aldi - ItemName ist nicht gefunden.");
            errorMessage.send(e.getMessage());
        }
        return "";
    }

    private String getImagePath(Document itemDoc, String name) {
        String href;
        try {
            href = itemDoc.select("img.img-responsive.cq-dd-image").first().attr("data-srcset").split(" ")[0];
        } catch (RuntimeException e) {
            log.error("!!! Aldi - ImageUrl ist nicht gefunden.");
            errorMessage.send(e.getMessage());
            return null;
        }
        name = name.replace(" ", "").replace("/", "").replace("-", "") + ".png";
        return Utils.downloadImage("https://www.aldi-nord.de" + (href), "aldi", Utils.getNextSaturday(), name);
    }

    private String getKategorieName(Element cat) {
        Elements h2 = cat.select("h2");
        if (h2.size() > 0) {
            try {

                return h2.first().ownText().split("\\p{Pd}")[1].replace("Traditionell genießen: ", "")
                        .replace("Frische Qualität in großer Auswahl: ", "")
                        .replace("Jetzt zugreifen! Nur solange der Vorrat reicht: ", "")
                        .replace("Marken aus unserem Sortiment.", "");
            } catch (ArrayIndexOutOfBoundsException e) {
                log.error("Kategorie nicht ermittelt");
            }
        }
        return "";
    }

    private LocalDate getLocalDate(Element el) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.GERMANY);
        return LocalDate.parse(el.attr("data-rel"), dateTimeFormatter);
    }

    @Override
    public String getDiscountName() {
        return "Aldi-Nord";
    }

    private Document getDocument(String url) {
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("!!! Url ist nicht erreichbar");
            errorMessage.send(e.getMessage());
        }
        return document;
    }
}
