package de.angebot.main.repositories;

import de.angebot.main.enities.Penny;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PennyRepo extends CrudRepository<Penny, Long> {

    @Override
    List<Penny> findAll();

}
