package de.angebot.main.repositories.discounters;

import de.angebot.main.enities.discounters.Netto;
import de.angebot.main.repositories.AbstractRepo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NettoRepo extends AbstractRepo<Netto, Long> {

    @Query(value = "SELECT a FROM Netto a WHERE a.bisDate >= CURRENT_DATE")
    List<Netto> findCurrentOffers();

    @Override
    List<Netto> findAll();

    @Modifying
    @Query(value= "delete from Netto a where a.bisDate >= CURRENT_DATE")
    void deleteAllActuel();


}
