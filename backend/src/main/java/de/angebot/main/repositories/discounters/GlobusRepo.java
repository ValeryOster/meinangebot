package de.angebot.main.repositories.discounters;

import de.angebot.main.enities.discounters.Globus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GlobusRepo extends CrudRepository<Globus, Long> {

    @Query(value = "SELECT a FROM Globus a WHERE a.bisDate >= CURRENT_DATE")
    List<Globus> findCurrentOffers();

    @Override
    List<Globus> findAll();

    @Modifying
    @Query(value= "delete from Globus a where a.bisDate >= CURRENT_DATE")
    void deleteAllActuel();
}
