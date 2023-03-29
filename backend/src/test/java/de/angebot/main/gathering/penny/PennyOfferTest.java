package de.angebot.main.gathering.penny;


import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class PennyOfferTest {

    @Autowired
    PennyOffer pennyOffer;

//    @Test
    public void startGathering() {
        pennyOffer.startGathering();

    }
}