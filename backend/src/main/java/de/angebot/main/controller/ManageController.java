package de.angebot.main.controller;


import de.angebot.main.common.ItemsCategory;
import de.angebot.main.enities.services.CommonGather;
import de.angebot.main.enities.discounters.Penny;
import de.angebot.main.repositories.discounters.PennyRepo;
import de.angebot.main.services.GatherService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manage")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ManageController {

    private final GatherService service;
    private final PennyRepo pennyRepo;

    public ManageController(GatherService service, PennyRepo pennyRepo) {
        this.service = service;
        this.pennyRepo = pennyRepo;
    }

    @PostMapping("/gather")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> startGathering(@RequestBody List<String> discounters) {
        Map<String, Object> result = service.startGather(discounters);
        boolean started = Boolean.TRUE.equals(result.get("started"));
        return started ? ResponseEntity.accepted().body(result)
                : ResponseEntity.status(HttpStatus.CONFLICT).body(result);
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getStatus() {
        return service.getStatus();
    }

    @GetMapping("/start")
    @PreAuthorize("hasRole('ADMIN')")
    public List<CommonGather> getAll() {
        return service.findAll();
    }

    @GetMapping("/sortkategorie")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Penny> getUnsortingKategorie() {
        return pennyRepo.findAllNotDefaultCategorieName(ItemsCategory.getKategorieList());
    }

    @PostMapping("/del")
    @PreAuthorize("hasRole('ADMIN')")
    public void delLastItems(@RequestBody List<String> discounters) {
        service.deleteLastInputs(discounters);
    }
}
