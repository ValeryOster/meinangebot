package de.angebot.main.repositories;

import de.angebot.main.enities.CommonGather;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommonGatherRepo extends CrudRepository<CommonGather, Long> {

    @Override
    List<CommonGather> findAll();
}
