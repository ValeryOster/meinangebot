package de.angebot.main.services;

import de.angebot.main.enities.*;
import de.angebot.main.enities.discounters.Aldi;
import de.angebot.main.enities.discounters.Lidl;
import de.angebot.main.enities.discounters.Netto;
import de.angebot.main.enities.discounters.Penny;
import de.angebot.main.repositories.discounters.AldiRepo;
import de.angebot.main.repositories.discounters.LidlRepo;
import de.angebot.main.repositories.discounters.NettoRepo;
import de.angebot.main.repositories.discounters.PennyRepo;
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


    public Map<String, List<? extends AbstactEneties>> getSelectedDiscounters(List<String> discounters) {
        return getOffersFromList(discounters);
    }

    private Map<String, List<? extends AbstactEneties>> getOffersFromList(List<String> discounters) {
        Map<String, List<? extends AbstactEneties>> discountersMap = new HashMap<>();
        for (String discounter : discounters) {
            if (discounter.toLowerCase().equals("lidl")) {
                List<Lidl> lidl = lidlCurrentOffers();
                if (lidl.size() > 0) {
                    discountersMap.put("Lidl", lidl);
                }
            }
            else if (discounter.toLowerCase().equals("penny")) {
                List<Penny> pennies = pennyCurrentOffers();
                if (pennies.size() > 0) {
                    discountersMap.put("Penny", pennies);
                }
            }
            else if (discounter.toLowerCase().equals("aldi")) {
                List<Aldi> aldiList = aldiCurrentOffers();
                if (aldiList.size() > 0) {
                    discountersMap.put("Aldi", aldiList);
                }
            }
            else if (discounter.toLowerCase().equals("netto")) {
                List<Netto> nettoList = nettoCurrentOffers();
                if (nettoList.size() > 0) {
                    discountersMap.put("Netto", nettoList);
                }
            }
        }
        return discountersMap;
    }
}
