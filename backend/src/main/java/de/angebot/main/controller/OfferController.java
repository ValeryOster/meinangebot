package de.angebot.main.controller;

import de.angebot.main.controller.dto.OfferDetailDto;
import de.angebot.main.controller.dto.OfferDto;
import de.angebot.main.controller.dto.PageResponse;
import de.angebot.main.services.OfferService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    public PageResponse<OfferDto> searchOffers(
            @RequestParam(defaultValue = "lidl") String retailer,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "discount") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        Page<OfferDto> result = offerService.searchOffers(retailer, search, category, sort, page, size);
        return PageResponse.from(result, dto -> dto);
    }

    @GetMapping("/{externalId}")
    public OfferDetailDto getOffer(@PathVariable String externalId) {
        return offerService.getOffer(externalId);
    }

    @GetMapping("/categories")
    public List<String> getCategories(@RequestParam(defaultValue = "lidl") String retailer) {
        return offerService.getCategories(retailer);
    }
}
