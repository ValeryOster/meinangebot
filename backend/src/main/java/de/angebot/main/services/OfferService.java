package de.angebot.main.services;

import de.angebot.main.controller.dto.OfferDetailDto;
import de.angebot.main.controller.dto.OfferDto;
import de.angebot.main.enities.offers.Offer;
import de.angebot.main.errors.ResourceNotFoundException;
import de.angebot.main.repositories.offers.OfferRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OfferService {

    private final OfferRepository offerRepository;

    public OfferService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    public Page<OfferDto> searchOffers(String retailer,
                                       String search,
                                       String category,
                                       String sort,
                                       int page,
                                       int size) {
        Specification<Offer> spec = buildSpecification(retailer, search, category);
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        return offerRepository.findAll(spec, pageable).map(OfferDto::fromEntity);
    }

    public OfferDetailDto getOffer(String externalId) {
        Offer offer = offerRepository.findById(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Angebot nicht gefunden: " + externalId));
        return OfferDetailDto.fromEntity(offer);
    }

    public List<String> getCategories(String retailer) {
        return offerRepository.findDistinctCategoriesByRetailer(retailer);
    }

    private Specification<Offer> buildSpecification(String retailer, String search, String category) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("retailer"), retailer));
            predicates.add(cb.or(
                    cb.isNull(root.get("validTo")),
                    cb.greaterThanOrEqualTo(root.get("validTo"), LocalDate.now())
            ));

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("brand")), pattern)
                ));
            }

            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort resolveSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "discountPercent");
        }
        return switch (sort.toLowerCase()) {
            case "price" -> Sort.by(Sort.Direction.ASC, "currentPrice");
            case "price-desc" -> Sort.by(Sort.Direction.DESC, "currentPrice");
            case "title" -> Sort.by(Sort.Direction.ASC, "title");
            case "validto" -> Sort.by(Sort.Direction.ASC, "validTo");
            default -> Sort.by(Sort.Direction.DESC, "discountPercent");
        };
    }
}
