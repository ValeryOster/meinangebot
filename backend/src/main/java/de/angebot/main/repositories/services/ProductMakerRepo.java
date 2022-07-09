package de.angebot.main.repositories.services;

import de.angebot.main.enities.ProductMaker;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductMakerRepo extends CrudRepository<ProductMaker,Long> {

    @Override
    List<ProductMaker> findAll();
}
