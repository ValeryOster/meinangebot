package de.angebot.main.services;

import de.angebot.main.enities.AbstactEneties;
import de.angebot.main.enities.Aldi;
import de.angebot.main.enities.Lidl;
import de.angebot.main.enities.Penny;
import de.angebot.main.gathering.MainGather;
import de.angebot.main.repositories.AldiRepo;
import de.angebot.main.repositories.CommonGatherRepo;
import de.angebot.main.repositories.LidlRepo;
import de.angebot.main.repositories.PennyRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DiscounterService {
    @Autowired
    private PennyRepo pennyRepo;

    @Autowired
    private LidlRepo lidlRepo;

    @Autowired
    private AldiRepo aldiRepo;

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

        return discounters;
    }
}
