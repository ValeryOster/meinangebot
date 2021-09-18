package de.angebot.main.repositories;

import de.angebot.main.enities.Aldi;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AldiRepo extends CrudRepository<Aldi, Long> {

    @Query(value = "SELECT a FROM Aldi a WHERE a.bisDate >= CURRENT_DATE")
    List<Aldi> findCurrentOffers();

    @Override
    List<Aldi> findAll();

    @Query(value= "delete from Aldi a where a.bisDate >= CURRENT_DATE")
    void deleteAllActuel();
}
