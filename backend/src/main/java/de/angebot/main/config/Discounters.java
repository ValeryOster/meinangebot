package de.angebot.main.config;

import de.angebot.main.gathering.common.Gathering;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class Discounters {

    @Autowired
    private Map<String, Gathering> discounters;

    public Discounters() {}

    public void setDiscounters(Map<String, Gathering> discounters) {
        this.discounters = discounters;
    }

    public List<String> getAllDiscountersName() {
        return discounters.keySet().stream().toList();
    }

    public List<Gathering> getAllDiscountersGather() {
        return discounters.values().stream().toList();
    }
}
