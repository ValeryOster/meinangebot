package de.angebot.main.schedules;

import de.angebot.main.gathering.lidl.LidlImportResult;
import de.angebot.main.services.LidlImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Zusätzlicher wöchentlicher Lidl-Import (Montag 04:00), ergänzend zum
 * globalen Discounter-Scheduler am Sonntag.
 */
@Slf4j
@Component
public class LidlImportSchedule {

    private final LidlImportService lidlImportService;

    public LidlImportSchedule(LidlImportService lidlImportService) {
        this.lidlImportService = lidlImportService;
    }

    @Scheduled(cron = "0 0 4 * * MON", zone = "Europe/Berlin")
    public void importLidlOffersWeekly() {
        log.info("Scheduled Lidl import started.");
        try {
            LidlImportResult result = lidlImportService.importCurrentOffers();
            log.info("Scheduled Lidl import completed: {}", result);
        } catch (RuntimeException e) {
            log.error("Scheduled Lidl import failed", e);
        }
    }
}
