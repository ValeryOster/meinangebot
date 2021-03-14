package de.angebot.main.controller;


import de.angebot.main.common.ItemsCategory;
import de.angebot.main.enities.CommonGather;
import de.angebot.main.enities.Penny;
import de.angebot.main.repositories.PennyRepo;
import de.angebot.main.services.GatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/manage")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ManageController {
    @Autowired
    private GatherService service;

    @Autowired
    private PennyRepo pennyRepo;

    @PostMapping("/gather")
    @PreAuthorize("hasRole('ADMIN')")
    public void startGathering(@RequestBody List<String> string) {
        service.startGather(string);
    }

    @GetMapping("/start")
    @PreAuthorize("hasRole('ADMIN')")
    public List<CommonGather> getAll(MultipartFile file) {
        return service.findAll();
    }

    @GetMapping("/sortkategorie")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Penny> getUnsortingKategorie() {
        return pennyRepo.findAllNotDefaultCategorieName(ItemsCategory.getKategorieList());
    }
}
