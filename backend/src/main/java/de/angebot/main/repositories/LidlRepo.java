package de.angebot.main.repositories;

import de.angebot.main.enities.Lidl;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LidlRepo extends CrudRepository<Lidl, Long> {
}
