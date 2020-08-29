package de.angebot.main.repositories;

import de.angebot.main.enities.Penny;
import org.springframework.data.repository.CrudRepository;

public interface PennyRepo extends CrudRepository<Penny, Long> {
}
