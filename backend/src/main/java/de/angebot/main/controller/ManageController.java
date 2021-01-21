package de.angebot.main.controller;


import de.angebot.main.common.ItemsCategory;
import de.angebot.main.enities.CommonGather;
import de.angebot.main.enities.Penny;
import de.angebot.main.gathering.MainGather;
import de.angebot.main.gathering.lidl.LidlOffer;
import de.angebot.main.gathering.penny.PennyOffer;
import de.angebot.main.repositories.CommonGatherRepo;
import de.angebot.main.repositories.LidlRepo;
import de.angebot.main.repositories.PennyRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    private LidlOffer lidlOffer;

    @Autowired
    private CommonGatherRepo gatherRepo;

    @Autowired
    private PennyRepo pennyRepo;

    @GetMapping("/gather")
    public void startGathering() {
        mainGather.setGatherRepo(gatherRepo);
        mainGather.addToGatherList(pennyOffer);
        mainGather.addToGatherList(lidlOffer);
        mainGather.startGather();
    }

    @GetMapping("/start")
    public List<CommonGather> getAll(MultipartFile file) {
        return gatherRepo.findAll();
    }

    @GetMapping("/sortkategorie")
    public List<Penny> getUnsortingKategorie() {
        return pennyRepo.findAllNotDefaultCategorieName(ItemsCategory.getKategorieList());
    }
}
