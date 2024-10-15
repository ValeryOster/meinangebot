package de.angebot.main.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface AbstractRepo<AbstactEneties,Long> extends CrudRepository<AbstactEneties, Long> {

    List<AbstactEneties> findCurrentOffers();


    void deleteAllActuel();
}
