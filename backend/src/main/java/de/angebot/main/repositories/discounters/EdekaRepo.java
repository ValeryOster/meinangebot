package de.angebot.main.repositories.discounters;

import de.angebot.main.enities.discounters.Edeka;
import de.angebot.main.repositories.AbstractRepo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EdekaRepo extends AbstractRepo<Edeka, Long> {

    @Query(value = "SELECT e FROM Edeka e WHERE e.bisDate >= CURRENT_DATE")
    List<Edeka> findCurrentOffers();

    @Override
    List<Edeka> findAll();

    @Modifying
    @Query(value= "delete from Edeka e where e.bisDate >= CURRENT_DATE")
    void deleteAllActuel();
}
