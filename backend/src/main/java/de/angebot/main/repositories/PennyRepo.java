package de.angebot.main.repositories;

import de.angebot.main.enities.Penny;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PennyRepo extends CrudRepository<Penny, Long> {

    @Override
    List<Penny> findAll();

    @Query(value = "SELECT p FROM Penny p WHERE p.bisDate >= CURRENT_DATE")
    List<Penny> findCurrentOffers();

    @Query(value = "SELECT p FROM Penny p WHERE p.kategorie not in :categorie")
    List<Penny> findAllNotDefaultCategorieName(@Param("categorie") List<String> kategorie);
}
