package de.angebot.main.controller;

import de.angebot.main.enities.*;
import de.angebot.main.enities.discounters.Aldi;
import de.angebot.main.enities.discounters.Lidl;
import de.angebot.main.enities.discounters.Netto;
import de.angebot.main.enities.discounters.Penny;
import de.angebot.main.services.DiscounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/home")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HomeController {
    @Autowired
    private DiscounterService service;

    @GetMapping(path = "/penny")
    public List<Penny> getPenny() {
        return service.pennyCurrentOffers();
    }

    @GetMapping(path = "/lidl")
    public List<Lidl> getLidl() {
        return service.lidlCurrentOffers();
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
        if (discounters.size() > 0) {
            Map<String, List<? extends AbstactEneties>> selectedDiscounters = service.getSelectedDiscounters(discounters);
            return selectedDiscounters;
        }
        return null;
    }
}
