package de.angebot.main.repositories;

import de.angebot.main.enities.Lidl;
import de.angebot.main.enities.Penny;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LidlRepo extends CrudRepository<Lidl, Long> {

    @Query(value = "SELECT l FROM Lidl l WHERE l.bisDate >= CURRENT_DATE")
    List<Lidl> findCurrentOffers();

    @Query(value= "delete from Lidl l where l.bisDate >= CURRENT_DATE")
    void deleteAllActuel();

    @Override
    List<Lidl> findAll();
}
