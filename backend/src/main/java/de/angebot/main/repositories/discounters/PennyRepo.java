package de.angebot.main.repositories.discounters;

import de.angebot.main.enities.discounters.Penny;
import de.angebot.main.repositories.AbstractRepo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PennyRepo extends AbstractRepo<Penny, Long> {

    @Override
    List<Penny> findAll();

    @Query(value = "SELECT p FROM Penny p WHERE p.bisDate >= CURRENT_DATE")
    List<Penny> findCurrentOffers();


    @Query(value= "delete from Penny p where p.bisDate >= CURRENT_DATE")
    void deleteAllActuel();
}
