package de.angebot.main.services;

import de.angebot.main.config.DiscounterEnum;
import de.angebot.main.config.DiscounterManager;
import de.angebot.main.controller.json.Item;
import de.angebot.main.enities.AbstactEneties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DiscounterService {
    private DiscounterManager manager;

    @Autowired
    public DiscounterService(DiscounterManager manager) {
        this.manager = manager;
    }


    public Map<DiscounterEnum, List<? extends AbstactEneties>> getSelectedDiscounters(List<DiscounterEnum> discounters) {
        return getOffersFromList(discounters);
    }


    public Map<DiscounterEnum, List<? extends AbstactEneties>> getOffersFromList(List<DiscounterEnum> discounters) {
        Map<DiscounterEnum, List<? extends AbstactEneties>> discountersMap = new HashMap<>();
        for (DiscounterEnum discounter : discounters) {
            discountersMap.put(discounter, manager.getDiscounter(discounter).findCurrentOffers());
        }
        return discountersMap;
    }

    public Item getSelectedItemById(Item item) {
        return null;
    }

    public DiscounterManager getManager() {
        return manager;
    }
}
