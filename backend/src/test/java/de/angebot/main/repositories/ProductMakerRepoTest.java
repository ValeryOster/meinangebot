package de.angebot.main.repositories;

import de.angebot.main.enities.ProductMaker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

//@SpringBootTest
class ProductMakerRepoTest {

    @Autowired
    private ProductMakerRepo productMakerRepo;

//    @Test
    public void shouldNotAllowToPresistNullStrings() {
        List<String> strings = Arrays.asList(
                "HP",
                "LENOVO",
                "HUAWEI",
                "GARMIN",
                "NINTENDO",
                "XIAOMI",
                "LEDLENSER",
                "BRAUN",
                "FACE-2-FACE",
                "NORTON"
        );

        for (String s : strings) {
            ProductMaker maker = new ProductMaker();
            maker.setMakerName(s);
            maker.setValid(true);

            productMakerRepo.save(maker);
        }
    }
}
