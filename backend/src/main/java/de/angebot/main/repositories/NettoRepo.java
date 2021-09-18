package de.angebot.main.repositories;

import de.angebot.main.enities.Aldi;
import de.angebot.main.enities.Netto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NettoRepo extends CrudRepository<Netto, Long> {

    @Query(value = "SELECT a FROM Netto a WHERE a.bisDate >= CURRENT_DATE")
    List<Netto> findCurrentOffers();

    @Override
    List<Netto> findAll();

    @Query(value= "delete from Netto a where a.bisDate >= CURRENT_DATE")
    void deleteAllActuel();
}
