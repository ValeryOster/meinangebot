package de.angebot.main.gathering.penny;


import de.angebot.main.enities.discounters.Penny;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.gathering.common.GatheringException;
import de.angebot.main.repositories.discounters.PennyRepo;
import de.angebot.main.utils.SaveUtil;
import de.angebot.main.utils.Utils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.time.temporal.WeekFields;
import java.util.stream.Stream;

@Slf4j
@Setter
@Component("penny")
public class PennyOffer extends Gathering {

    @Autowired
    private SaveUtil saveUtil;
    @Value("${penny.mainUrl}")
    private String mainUrl;
    @Autowired
    private PennyRepo pennyRepo;
    @Autowired
    private PennyApiClient pennyApiClient;


    @Override
    public int startGathering() {
        LocalDate startDate = Utils.getNextMonday();
        // Samstag derselben Angebotswoche (nicht "nächster Samstag ab heute": an einem Samstag wäre das heute)
        LocalDate endDate = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        WeekFields isoWeek = WeekFields.ISO;
        int year = startDate.get(isoWeek.weekBasedYear());
        int week = startDate.get(isoWeek.weekOfWeekBasedYear());

        return saveOffers(year, week, startDate, endDate);
    }

    private int saveOffers(int year, int week, LocalDate startDate, LocalDate endDate) {
        List<PennyOfferDto> offers = pennyApiClient.fetchOffers(year, week);
        if (offers.isEmpty()) {
            throw new GatheringException("Keine Penny-Angebote für KW " + year + "-" + week
                    + " gefunden (API nicht erreichbar oder leer, siehe Log).");
        }

        int saved = 0;
        for (PennyOfferDto offer : offers) {
            if (saveIfNotExists(toPenny(offer, startDate, endDate))) {
                saved++;
            }
        }
        log.info("Penny: {} Angebote gelesen, {} neu gespeichert.", offers.size(), saved);
        return saved;
    }

    private boolean saveIfNotExists(Penny penny) {
        boolean exists = pennyRepo.existsByProduktNameAndProduktMakerAndVonDateAndKategorie(
                penny.getProduktName(),
                penny.getProduktMaker(),
                penny.getVonDate(),
                penny.getKategorie()
        );
        if (exists) {
            log.info("Penny offer already exists and will be skipped: '{}' / '{}' / '{}' / '{}'",
                    penny.getProduktName(), penny.getProduktMaker(), penny.getVonDate(), penny.getKategorie());
            return false;
        }
        pennyRepo.save(penny);
        return true;
    }

    private String saveItemMaker(String strings) {
        if (!StringUtils.hasText(strings)) {
            return "";
        }
        saveUtil.saveProduktMaker(strings);
        return strings;
    }

    private Penny toPenny(PennyOfferDto offer, LocalDate startDate, LocalDate endDate) {
        Penny penny = new Penny();
        List<String> nameParts = Utils.splittToNameOrMaker(offer.getTitle());
        String productMaker = nameParts.get(0);
        String productName = nameParts.get(1).isBlank() ? offer.getTitle() : nameParts.get(1);
        String category = textPrettyPrint(offer.getCategory());

        penny.setVonDate(startDate);
        penny.setBisDate(endDate);
        penny.setImageLink(Utils.downloadImage(offer.getImageUrl(), "penny", endDate, ""));
        penny.setProduktMaker(saveItemMaker(productMaker));
        penny.setProduktName(productName);
        penny.setProduktPrise(offer.getPrice());
        penny.setProduktRegularPrise(offer.getRegularPrice());
        penny.setKategorie(category);
        penny.setUrl(toAbsoluteUrl(offer.getLinkHref()));

        return penny;
    }

    private String textPrettyPrint(String kategorie) {
        if (!StringUtils.hasText(kategorie)) {
            return "";
        }
        List<String> konnektor = Stream.of("und", "fuer", "seit").toList();
        StringBuilder prettyKategorie = new StringBuilder();
        String[] split = kategorie.replace("-", " ").split(" ");
        for (String word : split) {
            if (!konnektor.contains(word)) {
                prettyKategorie.append(" ").append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
            } else {
                prettyKategorie.append(" ").append(word);
            }
        }
        return prettyKategorie.toString().trim();
    }

    private String toAbsoluteUrl(String linkHref) {
        if (!StringUtils.hasText(linkHref)) {
            return "";
        }
        if (linkHref.startsWith("http")) {
            return linkHref;
        }
        return mainUrl + linkHref;
    }

    @Override
    public String getDiscountName() {
        return "Penny";
    }
}
