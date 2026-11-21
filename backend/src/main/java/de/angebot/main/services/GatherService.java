package de.angebot.main.services;

import de.angebot.main.config.Discounters;
import de.angebot.main.enities.services.CommonGather;
import de.angebot.main.gathering.MainGather;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.repositories.discounters.AldiRepo;
import de.angebot.main.repositories.discounters.EdekaRepo;
import de.angebot.main.repositories.discounters.NettoRepo;
import de.angebot.main.repositories.discounters.PennyRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class GatherService {

    private final MainGather mainGather;
    private final Discounters discounters;
    private final PennyRepo pennyRepo;
    private final AldiRepo aldiRepo;
    private final NettoRepo nettoRepo;
    private final EdekaRepo edekaRepo;

    public GatherService(MainGather mainGather,
                         Discounters discounters,
                         PennyRepo pennyRepo,
                         AldiRepo aldiRepo,
                         NettoRepo nettoRepo,
                         EdekaRepo edekaRepo) {
        this.mainGather = mainGather;
        this.discounters = discounters;
        this.pennyRepo = pennyRepo;
        this.aldiRepo = aldiRepo;
        this.nettoRepo = nettoRepo;
        this.edekaRepo = edekaRepo;
    }

    /**
     * Startet das Sammeln für die gewählten Discounter im Hintergrund.
     *
     * @return Statusmeldung für die Oberfläche
     */
    public Map<String, Object> startGather(List<String> names) {
        List<Gathering> selected = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        for (String name : names == null ? List.<String>of() : names) {
            discounters.findByName(name)
                    .filter(Gathering::isEnabled)
                    .ifPresentOrElse(selected::add, () -> skipped.add(name));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        if (selected.isEmpty()) {
            result.put("started", false);
            result.put("message", "Kein aktiver Discounter ausgewählt.");
        } else if (mainGather.startAsync(selected)) {
            result.put("started", true);
            result.put("message", "Sammeln gestartet: " + selected.stream().map(Gathering::getDiscountName).toList());
        } else {
            result.put("started", false);
            result.put("message", "Es läuft bereits ein Sammel-Lauf.");
        }
        if (!skipped.isEmpty()) {
            result.put("skipped", skipped);
        }
        return result;
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("running", mainGather.isRunning());
        status.put("current", mainGather.getCurrentDiscounter());
        status.put("startedAt", mainGather.getStartedAt());
        status.put("available", discounters.getEnabledDiscountersGather().stream()
                .map(Gathering::getDiscountName).sorted().toList());
        return status;
    }

    public List<CommonGather> findAll() {
        return mainGather.getGatherReport();
    }

    public void deleteLastInputs(List<String> discounters) {
        discounters.forEach(discounter -> {
            switch (discounter.toUpperCase(Locale.ROOT)) {
                case "PENNY" -> pennyRepo.deleteAllActuel();
                case "ALDI" -> aldiRepo.deleteAllActuel();
                case "NETTO" -> nettoRepo.deleteAllActuel();
                case "EDEKA" -> edekaRepo.deleteAllActuel();
                default -> log.warn("Unbekannter Discounter: {}", discounter);
            }
        });
    }
}
