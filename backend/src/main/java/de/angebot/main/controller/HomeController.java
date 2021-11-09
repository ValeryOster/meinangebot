package de.angebot.main.controller;

import de.angebot.main.enities.*;
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

    @GetMapping(path = "/aldi/{id}")
    public Aldi getAldiOfferById(@PathVariable Long id) {
        if (id != null) return service.getAldiOfferById(id);
        return null;
    }

    @GetMapping(path = "/lidl/{id}")
    public Lidl getLidlOfferById(@PathVariable Long id) {
        if (id != null) return service.getLidlOfferById(id);
        return null;
    }

    @GetMapping(path = "/penny/{id}")
    public Penny getPennyOfferById(@PathVariable Long id) {
        if (id != null) return service.getPennyOfferById(id);
        return null;
    }

    @PostMapping(path = "/auswahl")
    public Map<String, List<? extends AbstactEneties>> getAuswahlDiscouters(@RequestBody List<String> discounters) {
        if (discounters.size() > 0) {
            Map<String, List<? extends AbstactEneties>> auswahlDiscounters = service.getAuswahlDiscounters(discounters);
            return auswahlDiscounters;
        }
        return null;
    }
}
