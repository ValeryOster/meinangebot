package de.angebot.main.repositories.services;

import de.angebot.main.enities.services.CommonGather;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommonGatherRepo extends CrudRepository<CommonGather, Long> {

    @Override
    List<CommonGather> findAll();
}
