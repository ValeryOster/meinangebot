package de.angebot.main.common;

import de.angebot.main.gathering.aldi.AldiOffer;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.gathering.edeka.EdekaOffer;
import de.angebot.main.gathering.lidl.LidlOffer;
import de.angebot.main.gathering.netto.NettoOffer;
import de.angebot.main.gathering.penny.PennyOffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class Discounters {

    @Autowired
    private Map<String, ? extends Gathering> discounters = Map.of(
            "Penny", new PennyOffer(),
            "Lidl", new LidlOffer(),
            "Aldi", new AldiOffer(),
            "Netto", new NettoOffer(),
            "Edeka", new EdekaOffer());

    public Gathering getDiscounterByName(String discounterName) {
        for (String discounter : discounters.keySet()) {
            if (discounter.equals(discounterName)) {
                return discounters.get(discounter);
            }
        }
        throw new RuntimeException("Falsche Name vom Geschaeft");
    }
    public Set<String> getAllDiscountersName(){
        return discounters.keySet();
    }
    public List<? extends Gathering> getAllDiscountersGather() {
        return discounters.values().stream().toList();
    }
}
