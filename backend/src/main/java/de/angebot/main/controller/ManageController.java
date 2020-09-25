package de.angebot.main.controller;


import de.angebot.main.enities.CommonGather;
import de.angebot.main.gathering.MainGather;
import de.angebot.main.gathering.common.Gathering;
import de.angebot.main.gathering.penny.PennyOffer;
import de.angebot.main.repositories.CommonGatherRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/manage")
public class ManageController {
    private MainGather mainGather;

    @Autowired
    private CommonGatherRepo gatherRepo;

    public ManageController() {
        mainGather = new MainGather();
        mainGather.addToGatherList(new PennyOffer());
    }

    @GetMapping("/start")
    public List<CommonGather> getPenny() {
        mainGather.startGather();
        CommonGather gathering = new CommonGather();
        gathering.setGatherDate(LocalDate.now());
        gathering.setDicount(mainGather.getDiscountMap());
        gatherRepo.save(gathering);
        return gatherRepo.findAll();
    }
}
