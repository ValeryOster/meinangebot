package de.angebot.main.repositories;

import de.angebot.main.enities.ProductMaker;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class ProductMakerRepoTest {

    @Autowired
    private ProductMakerRepo productMakerRepo;

    @Test
    public void shouldNotAllowToPresistNullStrings() {
        List<String> strings = Arrays.asList(
                "PAMPERS",
                "FINISH",
                "GRANINI",
                "VEMONDO",
                "VALESS",
                "MIOMARE",
                "MERADISO",
                "PANASONIC",
                "YUM YUM",
                "GOLDEN SUN",
                "ALWAYS"
        );

        for (String s : strings) {
            ProductMaker maker = new ProductMaker();
            maker.setMakerName(s);
            maker.setValid(true);

                productMakerRepo.save(maker);


        }


    }
}
