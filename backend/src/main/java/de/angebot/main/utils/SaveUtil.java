package de.angebot.main.utils;

import de.angebot.main.enities.ProductMaker;
import de.angebot.main.repositories.services.ProductMakerRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

/** Speichert neue Herstellernamen (in Großbuchstaben) für die Herstellererkennung. */
@Slf4j
@Component
public class SaveUtil {

    private final ProductMakerRepo productMakerRepo;

    public SaveUtil(ProductMakerRepo productMakerRepo) {
        this.productMakerRepo = productMakerRepo;
    }

    public void saveProduktMaker(String produktMaker) {
        if (!StringUtils.hasText(produktMaker)) {
            return;
        }
        String name = produktMaker.trim().toUpperCase(Locale.GERMAN);
        if (productMakerRepo.existsByMakerName(name)) {
            return;
        }
        ProductMaker maker = new ProductMaker();
        maker.setMakerName(name);
        maker.setValid(true);
        try {
            productMakerRepo.save(maker);
        } catch (DataIntegrityViolationException e) {
            // gleichzeitig von einem anderen Lauf angelegt – kein Fehler
            log.debug("Hersteller {} existiert bereits.", name);
        }
    }
}
