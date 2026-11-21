package de.angebot.main.gathering;

import de.angebot.main.enities.services.CommonGather;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.repositories.services.CommonGatherRepo;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Führt das Sammeln der Angebote aus.
 * <p>
 * Es läuft immer höchstens ein Sammel-Lauf gleichzeitig. Manuelle Läufe werden im Hintergrund
 * gestartet, damit die HTTP-Anfrage sofort beantwortet wird. Das Ergebnis jedes Discounters
 * (Anzahl neuer Angebote oder Fehlermeldung) wird im Bericht {@link CommonGather} gespeichert.
 */
@Slf4j
@Component
public class MainGather {

    private static final int MAX_RESULT_LENGTH = 255;

    private final CommonGatherRepo gatherRepo;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "gathering");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile String currentDiscounter;
    private volatile LocalDateTime startedAt;

    public MainGather(CommonGatherRepo gatherRepo) {
        this.gatherRepo = gatherRepo;
    }

    /**
     * Startet einen Sammel-Lauf im Hintergrund.
     *
     * @return {@code false}, wenn bereits ein Lauf aktiv ist oder nichts zu sammeln ist
     */
    public boolean startAsync(Collection<Gathering> gatherings) {
        List<Gathering> toRun = enabledOnly(gatherings);
        if (toRun.isEmpty() || !running.compareAndSet(false, true)) {
            return false;
        }
        executor.submit(() -> runAndRelease(toRun));
        return true;
    }

    /**
     * Führt einen Sammel-Lauf im aufrufenden Thread aus (z. B. aus dem Zeitplan).
     *
     * @return {@code false}, wenn bereits ein Lauf aktiv ist oder nichts zu sammeln ist
     */
    public boolean runNow(Collection<Gathering> gatherings) {
        List<Gathering> toRun = enabledOnly(gatherings);
        if (toRun.isEmpty() || !running.compareAndSet(false, true)) {
            return false;
        }
        runAndRelease(toRun);
        return true;
    }

    public boolean isRunning() {
        return running.get();
    }

    public String getCurrentDiscounter() {
        return currentDiscounter;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public List<CommonGather> getGatherReport() {
        return gatherRepo.findAll();
    }

    private void runAndRelease(List<Gathering> gatherings) {
        try {
            run(gatherings);
        } catch (RuntimeException e) {
            log.error("Sammel-Lauf ist unerwartet abgebrochen", e);
        } finally {
            currentDiscounter = null;
            running.set(false);
        }
    }

    private void run(List<Gathering> gatherings) {
        startedAt = LocalDateTime.now();
        long start = System.currentTimeMillis();
        Map<String, String> results = new LinkedHashMap<>();

        for (Gathering gathering : gatherings) {
            String name = gathering.getDiscountName();
            currentDiscounter = name;
            log.info("++++++++++++ {} gestartet +++++++++++++++", name);
            try {
                int saved = gathering.startGathering();
                results.put(name, saved + " neue Angebote");
                log.info("++++++++++++ {} beendet: {} neue Angebote +++++++++++++++", name, saved);
            } catch (RuntimeException e) {
                results.put(name, truncate("FEHLER: " + e.getMessage()));
                log.error("Sammeln für {} fehlgeschlagen", name, e);
            }
        }

        long duration = System.currentTimeMillis() - start;
        saveGatheringReport(results, duration);
        log.info("Sammel-Lauf beendet am {}, Dauer {} ms, Ergebnis: {}", LocalDate.now(), duration, results);
    }

    private List<Gathering> enabledOnly(Collection<Gathering> gatherings) {
        Map<String, Gathering> unique = new LinkedHashMap<>();
        for (Gathering gathering : gatherings) {
            if (gathering == null) {
                continue;
            }
            if (!gathering.isEnabled()) {
                log.warn("{} ist deaktiviert und wird übersprungen.", gathering.getDiscountName());
                continue;
            }
            unique.putIfAbsent(gathering.getDiscountName(), gathering);
        }
        return new ArrayList<>(unique.values());
    }

    private void saveGatheringReport(Map<String, String> results, long duration) {
        CommonGather report = new CommonGather();
        report.setGatherDate(LocalDate.now());
        report.setDiscount(results);
        report.setDuration(duration);
        report.setExpiryDate(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY)));
        gatherRepo.save(report);
    }

    private static String truncate(String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= MAX_RESULT_LENGTH ? value : value.substring(0, MAX_RESULT_LENGTH - 1) + "…";
    }

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }
}
