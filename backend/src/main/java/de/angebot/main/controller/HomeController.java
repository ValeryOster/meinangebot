package de.angebot.main.controller;

import de.angebot.main.enities.*;
import de.angebot.main.enities.discounters.Aldi;
import de.angebot.main.enities.discounters.Netto;
import de.angebot.main.enities.discounters.Penny;
import de.angebot.main.services.DiscounterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/home")
public class HomeController {
    private final DiscounterService service;

    public HomeController(DiscounterService service) {
        this.service = service;
    }

    @GetMapping(path = "/penny")
    public List<Penny> getPenny() {
        return service.pennyCurrentOffers();
    }

    @GetMapping(path = "/aldi")
    public List<Aldi> getAldi() {
        return service.aldiCurrentOffers();
    }

    @GetMapping(path = "/netto")
    public List<Netto> getNetto() {
        return service.nettoCurrentOffers();
    }

    @PostMapping(path = "/auswahl")
    public Map<String, List<? extends AbstactEneties>> getSelectedDiscouters(@RequestBody List<String> discounters) {
        if (!discounters.isEmpty()) {
            return service.getSelectedDiscounters(discounters);
        }
        return Map.of();
    }
}
