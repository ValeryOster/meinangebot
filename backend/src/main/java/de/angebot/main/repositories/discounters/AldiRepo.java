package de.angebot.main.repositories.discounters;

import de.angebot.main.enities.discounters.Aldi;
import de.angebot.main.repositories.AbstractRepo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AldiRepo extends AbstractRepo<Aldi, Long> {

    @Query(value = "SELECT a FROM Aldi a WHERE a.bisDate >= CURRENT_DATE")
    List<Aldi> findCurrentOffers();

    @Override
    List<Aldi> findAll();

    @Modifying
    @Query(value= "delete from Aldi a where a.bisDate >= CURRENT_DATE")
    void deleteAllActuel();
}
