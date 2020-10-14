package de.angebot.main.controller;


import de.angebot.main.enities.CommonGather;
import de.angebot.main.gathering.MainGather;
import de.angebot.main.gathering.penny.PennyOffer;
import de.angebot.main.repositories.CommonGatherRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manage")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ManageController {
    @Autowired
    private MainGather mainGather;

    @Autowired
    private PennyOffer pennyOffer;

    @Autowired
    private CommonGatherRepo gatherRepo;

    @GetMapping("/gather")
    public void startGathering() {
        mainGather.setGatherRepo(gatherRepo);
        mainGather.addToGatherList(pennyOffer);
        mainGather.startGather();
    }

    @GetMapping("/start")
    public List<CommonGather> getAll() {
        return gatherRepo.findAll();
    }
}
