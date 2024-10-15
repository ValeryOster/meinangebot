package de.angebot.main.config;

import de.angebot.main.repositories.AbstractRepo;
import de.angebot.main.repositories.discounters.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
@Configuration
public class DiscounterManager {
   Map<DiscounterEnum, AbstractRepo> discounters = new HashMap<>();

    @Autowired
    public DiscounterManager(PennyRepo pennyRepo, LidlRepo lidlRepo, AldiRepo aldiRepo, NettoRepo nettoRepo, EdekaRepo edekaRepo) {
        discounters.put(DiscounterEnum.PENNY, pennyRepo);
        discounters.put(DiscounterEnum.LIDL, lidlRepo);
        discounters.put(DiscounterEnum.ALDI, aldiRepo);
        discounters.put(DiscounterEnum.NETTO, nettoRepo);
        discounters.put(DiscounterEnum.EDEKA, edekaRepo);
    }

    public AbstractRepo getDiscounter(DiscounterEnum discounter) {
        return discounters.get(discounter);
    }
}
