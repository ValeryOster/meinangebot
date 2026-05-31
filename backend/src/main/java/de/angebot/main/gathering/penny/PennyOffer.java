package de.angebot.main.gathering.penny;


import de.angebot.main.enities.discounters.Penny;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.repositories.discounters.PennyRepo;
import de.angebot.main.utils.SaveUtil;
import de.angebot.main.utils.Utils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.time.temporal.WeekFields;
import java.util.stream.Stream;

@Slf4j
@Setter
@Component("penny")
@Configuration
public class PennyOffer extends Gathering {

    @Autowired
    private SaveUtil saveUtil;
    @Value("${penny.mainUrl}")
    private String mainUrl;
    @Value("#{'${penny.categories:top-angebote}'.split(',')}")
    private List<String> categories;
    @Autowired
    private PennyRepo pennyRepo;
    @Autowired
    private PennyApiClient pennyApiClient;


    @Override
    public void startGathering() {
        LocalDate startDate = Utils.getNextMonday();
        LocalDate endDate = Utils.getNextSaturday();
        WeekFields isoWeek = WeekFields.ISO;
        int year = startDate.get(isoWeek.weekBasedYear());
        int week = startDate.get(isoWeek.weekOfWeekBasedYear());

        categories.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .forEach(category -> saveOffers(category, year, week, startDate, endDate));
    }

    private void saveOffers(String category, int year, int week, LocalDate startDate, LocalDate endDate) {
        List<PennyOfferDto> offers = pennyApiClient.fetchOffers(category, year, week);
        if (offers.isEmpty()) {
            log.error("No Penny offers found for category '{}' and week '{}-{}'.", category, year, week);
            return;
        }

        offers.stream()
                .map(offer -> toPenny(offer, startDate, endDate))
                .forEach(pennyRepo::save);
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

        penny.setVonDate(startDate);
        penny.setBisDate(endDate);
        penny.setImageLink(Utils.downloadImage(offer.getImageUrl(), "penny", endDate, ""));
        penny.setProduktMaker(saveItemMaker(nameParts.get(0)));
        penny.setProduktName(nameParts.get(1).isBlank() ? offer.getTitle() : nameParts.get(1));
        penny.setProduktPrise(offer.getPrice());
        penny.setProduktRegularPrise(offer.getRegularPrice());
        penny.setKategorie(textPrettyPrint(offer.getCategory()));
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
