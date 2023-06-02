package de.angebot.main.services;

import de.angebot.main.config.Discounters;
import de.angebot.main.enities.AbstactEneties;
import de.angebot.main.enities.discounters.*;
import de.angebot.main.repositories.discounters.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DiscounterService {
    private final PennyRepo pennyRepo;
    private final LidlRepo lidlRepo;
    private final AldiRepo aldiRepo;
    private final NettoRepo nettoRepo;
    private final EdekaRepo edekaRepo;


    @Autowired
    public DiscounterService(PennyRepo pennyRepo, LidlRepo lidlRepo, AldiRepo aldiRepo, NettoRepo nettoRepo, EdekaRepo edekaRepo, Discounters discounters) {
        this.pennyRepo = pennyRepo;
        this.lidlRepo = lidlRepo;
        this.aldiRepo = aldiRepo;
        this.nettoRepo = nettoRepo;
        this.edekaRepo = edekaRepo;
    }

    public List<Penny> pennyCurrentOffers() {
        return pennyRepo.findCurrentOffers();
    }

    public List<Lidl> lidlCurrentOffers() {
        return lidlRepo.findCurrentOffers();
    }

    public List<Aldi> aldiCurrentOffers() {
        return aldiRepo.findCurrentOffers();
    }

    public List<Netto> nettoCurrentOffers() {
        return nettoRepo.findCurrentOffers();
    }

    public List<Edeka> edekaCurrentOffers() {
        return edekaRepo.findCurrentOffers();
    }

    public Map<String, List<? extends AbstactEneties>> getSelectedDiscounters(List<String> discounters) {
        return  getOffersFromList(discounters);
    }


    public Map<String, List<? extends AbstactEneties>>  getOffersFromList(List<String> discounters) {
        Map<String, List<? extends AbstactEneties>> discountersMap = new HashMap<>();
        for (String discounter : discounters) {
            // TODO: 18.09.2022 Change to switch with Patter
            if (discounter.equalsIgnoreCase("lidl")) {
                List<Lidl> lidl = lidlCurrentOffers();
                if (lidl.size() > 0) {
                    discountersMap.put("Lidl", lidl);
                }
            }
            else if (discounter.equalsIgnoreCase("penny")) {
                List<Penny> pennies = pennyCurrentOffers();
                if (pennies.size() > 0) {
                    discountersMap.put("Penny", pennies);
                }
            }
            else if (discounter.equalsIgnoreCase("aldi")) {
                List<Aldi> aldiList = aldiCurrentOffers();
                if (aldiList.size() > 0) {
                    discountersMap.put("Aldi", aldiList);
                }
            }
            else if (discounter.equalsIgnoreCase("netto")) {
                List<Netto> nettoList = nettoCurrentOffers();
                if (nettoList.size() > 0) {
                    discountersMap.put("Netto", nettoList);
                }
            } else if (discounter.equalsIgnoreCase("edeka")) {
                List<Edeka> edekaList = edekaCurrentOffers();
                if (edekaList.size() > 0) {
                    discountersMap.put("Edeka", edekaList);
                }
            }
        }
        return discountersMap;
    }
}
