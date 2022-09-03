package de.angebot.main.gathering.globus;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class GlobusOfferTest {

    @Autowired
    GlobusOffer globusOffer;

    @Test
    public void startGathering() {
        globusOffer.startGathering();
    }
}