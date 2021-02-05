package de.angebot.main.controller;

import de.angebot.main.enities.AbstactEneties;
import de.angebot.main.enities.Lidl;
import de.angebot.main.enities.Penny;
import de.angebot.main.repositories.LidlRepo;
import de.angebot.main.repositories.PennyRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/home")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HomeController {
    @Autowired
    private PennyRepo pennyRepo;
    @Autowired
    private LidlRepo lidlRepo;

    @GetMapping(path = "/penny")
    public List<Penny> getPenny() {
        List<Penny> currentOffers = pennyRepo.findCurrentOffers();
        return currentOffers;
    }

    @GetMapping(path = "/lidl")
    public List<Lidl> getLidl() {
        return lidlRepo.findCurrentOffers();
    }

    @GetMapping(path = "/all")
    public Map<String, List<? extends AbstactEneties>> getAllDiscouters() {
        Map<String, List<? extends AbstactEneties>> discounters = new HashMap<>();
        discounters.put("Lidl", getLidl());
        discounters.put("Penny", getPenny());
        return discounters;
    }
    @GetMapping(path = "/apenny")
    public List<Penny> getAPenny() {
        List<Penny> currentOffers = pennyRepo.findAll();
        return currentOffers;
    }
}
