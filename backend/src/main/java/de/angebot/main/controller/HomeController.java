package de.angebot.main.controller;

import de.angebot.main.config.DiscounterEnum;
import de.angebot.main.controller.json.Item;
import de.angebot.main.enities.AbstactEneties;
import de.angebot.main.enities.discounters.*;
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
        return service.getManager().getDiscounter(DiscounterEnum.PENNY).findCurrentOffers();
    }

    @GetMapping(path = "/lidl")
    public List<Lidl> getLidl() {
        return service.getManager().getDiscounter(DiscounterEnum.LIDL).findCurrentOffers();
    }

    @GetMapping(path = "/aldi")
    public List<Aldi> getAldi() {
        return service.getManager().getDiscounter(DiscounterEnum.ALDI).findCurrentOffers();
    }

    @GetMapping(path = "/netto")
    public List<Netto> getNetto() {
        return service.getManager().getDiscounter(DiscounterEnum.NETTO).findCurrentOffers();
    }
 @GetMapping(path = "/edeka")
    public List<Edeka> getEdeka() {
        return service.getManager().getDiscounter(DiscounterEnum.EDEKA).findCurrentOffers();
    }

    @PostMapping(path = "/auswahl")
    public Map<DiscounterEnum, List<? extends AbstactEneties>> getSelectedDiscounters(@RequestBody List<DiscounterEnum> discounters) {
        if (discounters.size() > 0) {
            Map<DiscounterEnum, List<? extends AbstactEneties>> selectedDiscounters = service.getSelectedDiscounters(discounters);
            return selectedDiscounters;
        }
        return null;
    }

    @GetMapping(path = "/auswahl/{id}")
    public Item getSelectedItemById(@RequestBody Item item) {
        return service.getSelectedItemById(item);
    }
}
