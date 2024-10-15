package de.angebot.main.repositories.discounters;

import de.angebot.main.enities.discounters.Lidl;
import de.angebot.main.repositories.AbstractRepo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LidlRepo extends AbstractRepo<Lidl, Long> {

    @Query(value = "SELECT l FROM Lidl l WHERE l.bisDate >= CURRENT_DATE")
    List<Lidl> findCurrentOffers();

    @Query(value= "delete from Lidl l where l.bisDate >= CURRENT_DATE")
    void deleteAllActuel();

    @Override
    List<Lidl> findAll();
}
