package de.angebot.main.repositories.discounters;

import de.angebot.main.enities.discounters.Lidl;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LidlRepo extends CrudRepository<Lidl, Long> {

    Optional<Lidl> findFirstByUrl(String url);

    @Query(value = "SELECT l FROM Lidl l WHERE l.bisDate >= CURRENT_DATE")
    List<Lidl> findCurrentOffers();

    @Query(value= "delete from Lidl l where l.bisDate >= CURRENT_DATE")
    void deleteAllActuel();

    @Override
    List<Lidl> findAll();
}
