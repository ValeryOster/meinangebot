package de.angebot.main.controller;

import de.angebot.main.enities.Penny;
import de.angebot.main.repositories.PennyRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/home")
public class HomeController {
    @Autowired
    private PennyRepo pennyRepo;

    @GetMapping(path = "/penny")
    public List<Penny> getPenny() {
        return pennyRepo.findAll();
    }
}
