package de.angebot.main.services;

import de.angebot.main.enities.*;
import de.angebot.main.repositories.AldiRepo;
import de.angebot.main.repositories.LidlRepo;
import de.angebot.main.repositories.NettoRepo;
import de.angebot.main.repositories.PennyRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class DiscounterService {
    @Autowired
    private PennyRepo pennyRepo;

    @Autowired
    private LidlRepo lidlRepo;

    @Autowired
    private AldiRepo aldiRepo;

    @Autowired
    private NettoRepo nettoRepo;

    public List<Penny> pennyCurrentOffers() {
        return pennyRepo.findCurrentOffers();
    }
    public Penny getPennyOfferById(Long id) {
        try {
            return pennyRepo.findById(id).get();
        } catch (NoSuchElementException e) {
            return new Penny();
        }
    }

    public List<Lidl> lidlCurrentOffers() {
        return lidlRepo.findCurrentOffers();
    }

    public Lidl getLidlOfferById(Long id) {
        try {
            return lidlRepo.findById(id).get();
        } catch (NoSuchElementException e) {
            return new Lidl();
        }
    }

    public List<Aldi> aldiCurrentOffers() {
        return aldiRepo.findCurrentOffers();
    }

    public Aldi getAldiOfferById(Long id) {
        try {
            return aldiRepo.findById(id).get();
        } catch (NoSuchElementException e) {
            return new Aldi();
        }
    }

    public List<Netto> nettoCurrentOffers() {
        return nettoRepo.findCurrentOffers();
    }

    public Map<String, List<? extends AbstactEneties>> getAllCurrentOffers() {
        Map<String, List<? extends AbstactEneties>> discounters = new HashMap<>();
        List<Lidl> lidl = lidlCurrentOffers();
        if (lidl.size() > 0) {
            discounters.put("Lidl", lidl);
        }
        List<Penny> pennies = pennyCurrentOffers();
        if (pennies.size() > 0) {
            discounters.put("Penny", pennies);
        }

        List<Aldi> aldiList = aldiCurrentOffers();
        if (aldiList.size() > 0) {
            discounters.put("Aldi", aldiList);
        }

        List<Netto> nettoList = nettoCurrentOffers();
        if (nettoList.size() > 0) {
            discounters.put("Netto", nettoList);
        }

        return discounters;
    }
}
