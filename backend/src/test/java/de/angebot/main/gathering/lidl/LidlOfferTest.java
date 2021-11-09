package de.angebot.main.gathering.lidl;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class LidlOfferTest {

    @Autowired
    LidlOffer lidlOffer;

    @Test
    void startGathering() {
        lidlOffer.startGathering();
    }

//    @Test
    void testFuture() {
        String a = "Gültig bis Mi., 16.12.";
        String dateText = a.replaceAll("(.*)(\\d{2}.\\d{2}.)", "$2");
        System.out.println(Character.isDigit(dateText.charAt(0)));
    }

}
