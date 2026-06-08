package de.angebot.main.gathering.lidl;

import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.services.LidlImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("lidl")
public class LidlOffer extends Gathering {

    private final LidlImportService lidlImportService;

    public LidlOffer(LidlImportService lidlImportService) {
        this.lidlImportService = lidlImportService;
    }

    @Override
    public void startGathering() {
        LidlImportResult result = lidlImportService.importCurrentOffers();
        log.info("Lidl gather result: parsed={}, campaignParsed={}, leafletParsed={}, inserted={}, updated={}, "
                        + "duplicates={}, skipped={}, failed={}, campaignsProcessed={}, leafletsProcessed={}, durationMs={}",
                result.getParsed(), result.getCampaignParsed(), result.getLeafletParsed(),
                result.getInserted(), result.getUpdated(), result.getDuplicates(), result.getSkipped(),
                result.getFailed(), result.getCampaignsProcessed(), result.getLeafletsProcessed(), result.getDurationMs());
    }

    @Override
    public String getDiscountName() {
        return "Lidl";
    }
}
