package de.angebot.main.repositories.offers;

import de.angebot.main.enities.offers.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, String>, JpaSpecificationExecutor<Offer> {

    @Query("SELECT DISTINCT o.category FROM Offer o WHERE o.retailer = :retailer AND o.category IS NOT NULL "
            + "AND o.category <> '' AND (o.validTo IS NULL OR o.validTo >= CURRENT_DATE) ORDER BY o.category")
    List<String> findDistinctCategoriesByRetailer(String retailer);
}
