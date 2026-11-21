package de.angebot.main.config;

import de.angebot.main.gathering.common.Gathering;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Alle Sammler, nach Bean-Namen ("penny", "aldi", "netto", "edeka"). */
@Component
public class Discounters {

    private final Map<String, Gathering> discounters;

    public Discounters(Map<String, Gathering> discounters) {
        this.discounters = discounters;
    }

    public List<String> getAllDiscountersName() {
        return discounters.keySet().stream().sorted().toList();
    }

    public List<Gathering> getAllDiscountersGather() {
        return discounters.values().stream().toList();
    }

    public List<Gathering> getEnabledDiscountersGather() {
        return discounters.values().stream().filter(Gathering::isEnabled).toList();
    }

    public Optional<Gathering> findByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(discounters.get(name.trim().toLowerCase(Locale.ROOT)));
    }
}
