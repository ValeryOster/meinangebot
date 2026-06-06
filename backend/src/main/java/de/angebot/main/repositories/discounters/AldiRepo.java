package de.angebot.main.repositories.discounters;

import de.angebot.main.enities.discounters.Aldi;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AldiRepo extends CrudRepository<Aldi, Long> {

    @Query(value = "SELECT a FROM Aldi a WHERE a.bisDate >= CURRENT_DATE")
    List<Aldi> findCurrentOffers();

    @Override
    List<Aldi> findAll();

    boolean existsByUrlAndVonDateAndKategorie(String url, LocalDate vonDate, String kategorie);

    Optional<Aldi> findByUrlAndVonDateAndKategorie(String url, LocalDate vonDate, String kategorie);

    @Modifying
    @Query(value= "delete from Aldi a where a.bisDate >= CURRENT_DATE")
    void deleteAllActuel();
}
