package de.angebot.main.gathering.netto;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class NettoOfferTest {

    @Autowired
    NettoOffer nettoOffer;

    @Test
    void startGathering() {
        nettoOffer.startGathering();
    }
}
