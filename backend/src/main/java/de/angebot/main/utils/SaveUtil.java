package de.angebot.main.utils;

import de.angebot.main.enities.ProductMaker;
import de.angebot.main.repositories.services.ProductMakerRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SaveUtil {

    @Autowired
    private ProductMakerRepo productMakerRepo;

    public void saveProduktMaker(String produktMaker) {
        try {
            produktMaker = produktMaker.toUpperCase();
            ProductMaker maker = new ProductMaker();
            maker.setMakerName(produktMaker);
            maker.setValid(true);
            productMakerRepo.save(maker);
        } catch (DataIntegrityViolationException e) {
            log.info("Der Hersteller: " + produktMaker + " ist in DB ");
        }
    }
}
